cmake_minimum_required(VERSION 3.6.0)

project(cryptopp LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSION OFF)

file(GLOB_RECURSE Include_Dir LIST_DIRECTORIES true ".")

include_directories(${Include_Dir})
file(GLOB_RECURSE Src_Files "*.cpp")

add_library(${PROJECT_NAME} STATIC ${Src_Files})
set_target_properties(${PROJECT_NAME} PROPERTIES LINKER_LANGUAGE CXX)

