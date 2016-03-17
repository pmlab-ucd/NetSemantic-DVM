package fu.hao.trust.analysis;

import java.lang.reflect.Method;
import java.util.Map;

import fu.hao.trust.dvm.DalvikVM;
import patdroid.dalvik.Instruction;

interface Rule {
    Map<Object, Method> flow(DalvikVM vm, Instruction inst, Map<Object, Method> in);
}