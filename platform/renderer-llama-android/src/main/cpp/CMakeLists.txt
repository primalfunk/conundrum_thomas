cmake_minimum_required(VERSION 3.22.1)

project(thomas_renderer_llama LANGUAGES C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)
add_compile_options(-O3 -ffunction-sections -fdata-sections)
add_link_options(-Wl,--gc-sections)

set(LLAMA_CPP_SOURCE_DIR "${CMAKE_CURRENT_LIST_DIR}/../../../../../third_party/llama.cpp")
if(NOT EXISTS "${LLAMA_CPP_SOURCE_DIR}/include/llama.h")
    message(FATAL_ERROR "Pinned llama.cpp submodule is missing")
endif()

set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_COMMON OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TOOLS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_APP OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_UI OFF CACHE BOOL "" FORCE)
set(LLAMA_OPENSSL OFF CACHE BOOL "" FORCE)
set(GGML_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(GGML_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "" FORCE)
set(GGML_NATIVE OFF CACHE BOOL "" FORCE)
set(GGML_LLAMAFILE OFF CACHE BOOL "" FORCE)
set(GGML_CPU_KLEIDIAI OFF CACHE BOOL "" FORCE)
set(GGML_VULKAN OFF CACHE BOOL "" FORCE)
set(GGML_OPENCL OFF CACHE BOOL "" FORCE)
set(GGML_RPC OFF CACHE BOOL "" FORCE)

add_subdirectory("${LLAMA_CPP_SOURCE_DIR}" "${CMAKE_CURRENT_BINARY_DIR}/llama")

add_library(thomas_renderer_llama SHARED thomas_renderer_jni.cpp)
target_compile_definitions(thomas_renderer_llama PRIVATE
    THOMAS_LLAMA_RELEASE="b10621"
    THOMAS_LLAMA_COMMIT="c1d0e7a004015f23bc0233470b747b596f29b264"
)
target_include_directories(thomas_renderer_llama PRIVATE
    "${LLAMA_CPP_SOURCE_DIR}/include"
    "${LLAMA_CPP_SOURCE_DIR}/ggml/include"
)
target_link_libraries(thomas_renderer_llama PRIVATE llama android log)
