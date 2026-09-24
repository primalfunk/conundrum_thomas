#include <android/log.h>
#include <jni.h>

#include <algorithm>
#include <atomic>
#include <mutex>
#include <sstream>
#include <stdexcept>
#include <string>
#include <vector>

#include "llama.h"

namespace {

constexpr int kContextLength = 4096;
constexpr int kBatchSize = 512;
constexpr int kThreads = 4;

struct Engine {
    std::mutex mutex;
    llama_model * model = nullptr;
    llama_context * context = nullptr;
    std::atomic_bool generating{false};
};

Engine g_engine;

std::string from_java(JNIEnv * env, jstring value) {
    if (value == nullptr) return {};
    const char * utf8 = env->GetStringUTFChars(value, nullptr);
    if (utf8 == nullptr) throw std::runtime_error("Unable to read Java string");
    std::string result(utf8);
    env->ReleaseStringUTFChars(value, utf8);
    return result;
}

void release_locked() {
    if (g_engine.context != nullptr) {
        llama_free(g_engine.context);
        g_engine.context = nullptr;
    }
    if (g_engine.model != nullptr) {
        llama_model_free(g_engine.model);
        g_engine.model = nullptr;
    }
}

std::vector<llama_token> tokenize(const llama_vocab * vocab, const std::string & text) {
    const int32_t required = llama_tokenize(
        vocab, text.data(), static_cast<int32_t>(text.size()), nullptr, 0, true, true);
    if (required == INT32_MIN) throw std::runtime_error("Prompt token count overflow");
    const int32_t count = required < 0 ? -required : required;
    std::vector<llama_token> tokens(static_cast<size_t>(count));
    const int32_t written = llama_tokenize(
        vocab, text.data(), static_cast<int32_t>(text.size()), tokens.data(), count, true, true);
    if (written < 0) throw std::runtime_error("Prompt tokenization failed");
    tokens.resize(static_cast<size_t>(written));
    return tokens;
}

std::string token_piece(const llama_vocab * vocab, llama_token token) {
    int32_t required = llama_token_to_piece(vocab, token, nullptr, 0, 0, true);
    if (required < 0) required = -required;
    std::string piece(static_cast<size_t>(required), '\0');
    const int32_t written = llama_token_to_piece(
        vocab, token, piece.data(), required, 0, true);
    if (written < 0) throw std::runtime_error("Token decoding failed");
    piece.resize(static_cast<size_t>(written));
    return piece;
}

std::string format_prompt(const std::string & system, const std::string & user) {
    const char * tmpl = llama_model_chat_template(g_engine.model, nullptr);
    if (tmpl == nullptr) throw std::runtime_error("Governed model has no chat template");
    const llama_chat_message messages[] = {
        {"system", system.c_str()},
        {"user", user.c_str()},
    };
    int32_t required = llama_chat_apply_template(tmpl, messages, 2, true, nullptr, 0);
    if (required < 0) required = -required;
    if (required <= 0) throw std::runtime_error("Chat template produced no prompt");
    std::string formatted(static_cast<size_t>(required), '\0');
    const int32_t written = llama_chat_apply_template(
        tmpl, messages, 2, true, formatted.data(), required);
    if (written < 0) throw std::runtime_error("Chat template application failed");
    formatted.resize(static_cast<size_t>(written));
    return formatted;
}

void decode_tokens(const std::vector<llama_token> & tokens) {
    size_t offset = 0;
    while (offset < tokens.size()) {
        const int32_t count = static_cast<int32_t>(
            std::min(tokens.size() - offset, static_cast<size_t>(kBatchSize)));
        llama_batch batch = llama_batch_get_one(
            const_cast<llama_token *>(tokens.data() + offset),
            count);
        if (llama_decode(g_engine.context, batch) != 0) {
            throw std::runtime_error("Prompt decoding failed");
        }
        offset += static_cast<size_t>(count);
    }
}

jstring result_or_throw(JNIEnv * env, const std::string & value) {
    return env->NewStringUTF(value.c_str());
}

void throw_state(JNIEnv * env, const std::string & message) {
    jclass type = env->FindClass("java/lang/IllegalStateException");
    if (type != nullptr) env->ThrowNew(type, message.c_str());
}

void throw_argument(JNIEnv * env, const std::string & message) {
    jclass type = env->FindClass("java/lang/IllegalArgumentException");
    if (type != nullptr) env->ThrowNew(type, message.c_str());
}

} // namespace

extern "C" JNIEXPORT jstring JNICALL
Java_com_conundrum_thomas_v2_platform_renderer_llama_NativeLlamaBridge_nativeRuntimeIdentity(
    JNIEnv * env, jobject) {
    return result_or_throw(env, std::string(THOMAS_LLAMA_RELEASE) + "\t" +
        THOMAS_LLAMA_COMMIT + "\t" + llama_version() + "\tarm64-v8a\tCPU");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_conundrum_thomas_v2_platform_renderer_llama_NativeLlamaBridge_nativeLoad(
    JNIEnv * env, jobject, jstring path) {
    try {
        const std::string model_path = from_java(env, path);
        if (model_path.empty()) throw std::invalid_argument("Model path is empty");
        std::lock_guard<std::mutex> lock(g_engine.mutex);
        release_locked();
        llama_model_params model_params = llama_model_default_params();
        model_params.n_gpu_layers = 0;
        model_params.load_mode = LLAMA_LOAD_MODE_MMAP;
        g_engine.model = llama_model_load_from_file(model_path.c_str(), model_params);
        if (g_engine.model == nullptr) throw std::runtime_error("llama.cpp model load failed");
        llama_context_params context_params = llama_context_default_params();
        context_params.n_ctx = kContextLength;
        context_params.n_batch = kBatchSize;
        context_params.n_ubatch = kBatchSize;
        context_params.n_threads = kThreads;
        context_params.n_threads_batch = kThreads;
        g_engine.context = llama_init_from_model(g_engine.model, context_params);
        if (g_engine.context == nullptr) {
            release_locked();
            throw std::runtime_error("llama.cpp context creation failed");
        }
        char description[256] = {};
        llama_model_desc(g_engine.model, description, sizeof(description));
        return result_or_throw(env, description);
    } catch (const std::exception & error) {
        release_locked();
        throw_state(env, error.what());
        return nullptr;
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_conundrum_thomas_v2_platform_renderer_llama_NativeLlamaBridge_nativeGenerate(
    JNIEnv * env, jobject, jstring system_prompt, jstring user_prompt, jint maximum_tokens) {
    try {
        if (maximum_tokens < 1 || maximum_tokens > 256) {
            throw std::invalid_argument("Maximum generation length is outside the governed range");
        }
        std::lock_guard<std::mutex> lock(g_engine.mutex);
        if (g_engine.model == nullptr || g_engine.context == nullptr) {
            throw std::runtime_error("Governed model is not loaded");
        }
        if (g_engine.generating.exchange(true)) {
            throw std::runtime_error("A local generation is already active");
        }
        struct GenerationGuard {
            ~GenerationGuard() { g_engine.generating.store(false); }
        } guard;
        const std::string formatted = format_prompt(from_java(env, system_prompt), from_java(env, user_prompt));
        const auto prompt_tokens = tokenize(llama_model_get_vocab(g_engine.model), formatted);
        if (prompt_tokens.empty() || prompt_tokens.size() + static_cast<size_t>(maximum_tokens) > kContextLength) {
            throw std::runtime_error("Governed prompt exceeds the pinned context length");
        }
        llama_memory_clear(llama_get_memory(g_engine.context), true);
        decode_tokens(prompt_tokens);
        llama_sampler * sampler = llama_sampler_chain_init(llama_sampler_chain_default_params());
        if (sampler == nullptr) throw std::runtime_error("Sampler creation failed");
        llama_sampler_chain_add(sampler, llama_sampler_init_greedy());
        std::string output;
        const auto * vocab = llama_model_get_vocab(g_engine.model);
        for (int token_index = 0; token_index < maximum_tokens; ++token_index) {
            const llama_token token = llama_sampler_sample(sampler, g_engine.context, -1);
            if (llama_vocab_is_eog(vocab, token)) break;
            llama_sampler_accept(sampler, token);
            output += token_piece(vocab, token);
            llama_token next = token;
            llama_batch batch = llama_batch_get_one(&next, 1);
            if (llama_decode(g_engine.context, batch) != 0) {
                llama_sampler_free(sampler);
                throw std::runtime_error("Generation decoding failed");
            }
        }
        llama_sampler_free(sampler);
        return result_or_throw(env, output);
    } catch (const std::invalid_argument & error) {
        throw_argument(env, error.what());
        return nullptr;
    } catch (const std::exception & error) {
        throw_state(env, error.what());
        return nullptr;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_conundrum_thomas_v2_platform_renderer_llama_NativeLlamaBridge_nativeUnload(
    JNIEnv *, jobject) {
    std::lock_guard<std::mutex> lock(g_engine.mutex);
    release_locked();
}

extern "C" jint JNI_OnLoad(JavaVM *, void *) {
    llama_backend_init();
    return JNI_VERSION_1_6;
}

extern "C" void JNI_OnUnload(JavaVM *, void *) {
    std::lock_guard<std::mutex> lock(g_engine.mutex);
    release_locked();
    llama_backend_free();
}
