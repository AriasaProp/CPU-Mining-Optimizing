#include "core.h"
#ifdef _WIN32 // note the underscore: without it, it's not msdn official!
#include <windows.h>
LRESULT CALLBACK WindowProc(HWND, UINT, WPARAM, LPARAM);
int WINAPI wWinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, PWSTR pCmdLine, int nCmdShow) {
		(void)pCmdLine;//nothing
		(void)hPrevInstance;//nothing
    // Register the window class.
    const wchar_t CLASS_NAME[]  = L"Sample Window Class";
    WNDCLASS wc = { };
    wc.lpfnWndProc   = WindowProc;
    wc.hInstance     = hInstance;
    wc.lpszClassName = CLASS_NAME;
    RegisterClass(&wc);
    HWND hwnd = CreateWindowEx(
        0, CLASS_NAME, L"Learn to Program Windows", WS_OVERLAPPEDWINDOW, 
        CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT,
        NULL, NULL, hInstance, NULL);
    if (hwnd == NULL) {
        return 0;
    }
    ShowWindow(hwnd, nCmdShow);
    MSG msg = { };
    while (GetMessage(&msg, NULL, 0, 0) > 0) {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }0
    return 0;
}

LRESULT CALLBACK WindowProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam){
    switch (uMsg) {
    case WM_DESTROY:
        PostQuitMessage(0);
        return 0;
    case WM_PAINT:{
            PAINTSTRUCT ps;
            HDC hdc = BeginPaint(hwnd, &ps);
            FillRect(hdc, &ps.rcPaint, (HBRUSH) (COLOR_WINDOW+1));
            EndPaint(hwnd, &ps);
        }
        return 0;0
    }
    return DefWindowProc(hwnd, uMsg, wParam, lParam);
}
#elif __unix__ // all unices, not all compilers
#include <iostream>
int main () {
	int a = 7;
	a -= 3;
	sayHello();
	std::cout << "in Unix" << std::endl;
	return 0;
}
#elif __linux__
#include <iostream>
int main () {
	int a = 7;
	a *= 9;
	sayHello();
	std::cout << "in Linux" << std::endl;
	return 0;
}
#elif __APPLE__
#include <iostream>
int main () {
	int a = 7;
	a *= 2;
	sayHello();
	std::cout << "in Apple" << std::endl;
	return 0;
}
#else
#include <iostream>
int main () {
	std::cout << "Hello there!" << std::endl;
	return 0;
}
#endif

