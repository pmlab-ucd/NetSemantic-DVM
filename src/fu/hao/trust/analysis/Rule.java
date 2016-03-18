package fu.hao.trust.analysis;

import java.util.Map;

import fu.hao.trust.dvm.DalvikVM;
import patdroid.dalvik.Instruction;

interface Rule {
    Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in);
}