package fu.hao.trust.dvm;

import patdroid.dalvik.Instruction;

interface ByteCode {
    void func(DalvikVM vm, Instruction inst);
}
