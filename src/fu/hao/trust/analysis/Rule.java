package fu.hao.trust.analysis;

import java.util.Map;

import fu.hao.trust.dvm.DalvikVM;
import patdroid.dalvik.Instruction;

interface Rule {
    Map<String, Map<Object, Instruction>> flow(DalvikVM vm, Instruction inst, Map<String, Map<Object, Instruction>> ins);
}