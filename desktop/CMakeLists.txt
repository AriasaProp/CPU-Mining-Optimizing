cmake_minimum_required (VERSION 3.6.0)

project(DesktopApp LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSION OFF)

set(CORE_SRC "../core")

file(GLOB_RECURSE Include_subDir LIST_DIRECTORIES true ${CORE_SRC})
file(GLOB_RECURSE Include_Dir LIST_DIRECTORIES true ".")
include_directories(${Include_Dir})
include_directories(${Include_subDir})

add_subdirectory(${CORE_SRC} bin EXCLUDE_FROM_ALL)

set(CMAKE_CXX_FLAGS "-Wall -Wextra -O3 -Wpedantic")
add_executable(${PROJECT_NAME} desktop_main.cpp)

target_link_libraries(${PROJECT_NAME} PUBLIC core)
set_target_properties(${PROJECT_NAME} PROPERTIES LINKER_LANGUAGE CXX)

install(TARGETS ${PROJECT_NAME}
				DESTINATION bin
        ARCHIVE DESTINATION lib
        LIBRARY DESTINATION lib
        RUNTIME DESTINATION bin)

