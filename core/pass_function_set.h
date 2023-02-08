#ifndef Pass_Function_Set_Included
#define Pass_Function_Set_Included

namespace function_set {
	//trigger mining
	extern void (*afterStart) ();
	extern void (*afterStop) ();
	//socket connection
	//return false cause error or has connection 
	extern bool (*openConnection) (const char*,const unsigned int);
	//return false cause error or no connection 
	extern bool (*closeConnection) ();
	//console receiver msg
	extern void (*consoleMessage) (const char*,const unsigned int);
}

#endif //Pass_Function_Set_Included