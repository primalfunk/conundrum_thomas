# `:platform:renderer-llama-android`

Reserved Android llama-family renderer adapter boundary. It targets `arm64-v8a`
for the development baseline and may implement only the narrow governed
realizer contract. It still contains no JNI, llama.cpp, model, model adapter, or
renderer admission in CT-V2-15; production uses the deterministic CT-V2-13
reference realizer.
