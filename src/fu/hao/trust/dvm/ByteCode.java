package fu.hao.trust.dvm;

import patdroid.dalvik.Instruction;

public interface ByteCode {
    void func(DalvikVM vm, Instruction inst);
}
