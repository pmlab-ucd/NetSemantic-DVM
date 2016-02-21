package fu.hao.trust.analysis;

import java.util.Set;

import fu.hao.trust.dvm.DalvikVM;
import patdroid.dalvik.Instruction;

interface Rule {
    Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in);
}