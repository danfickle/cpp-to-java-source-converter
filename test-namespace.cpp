// Test function outside namespace...
int func_outside_namespace();

// Test class outside namespace...
class class_outside_namespace
{

};

// Test variables outside namespace...
class_outside_namespace cls1;
int variable;

// Test a namespace...
namespace mynamespace
{
	class class_inside_namespace { int i; };

	class_inside_namespace cls2;
	int var;
};
