package io.github.maritims.chip8j.cpu;

import io.github.maritims.chip8j.Memory;
import io.github.maritims.chip8j.Observable;
import io.github.maritims.chip8j.Observer;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.function.IntUnaryOperator;

public class State implements Observable {
    private final Memory         memory;
    private final PixelBuffer    pixelBuffer = new PixelBuffer(64, 32);
    private final Registers      registers;
    private final Stack<Integer> stack       = new Stack<>();

    private int     index          = 0;
    private int     programCounter = 0x200;
    private int     delayTimer;
    private int     soundTimer;
    private boolean drawFlag;
    private int     rawOpcode;
    private Opcode  opcode;
    private int     x;
    private int     y;
    private int     n;
    private int     nn;
    private int     nnn;


    public State(Memory memory, Registers registers) {
        this.memory    = memory;
        this.registers = registers;
    }

    void updateInstruction(int rawOpcode, Opcode opcode, int x, int y, int n, int nn, int nnn) {
        this.rawOpcode = rawOpcode;
        this.opcode    = opcode;
        this.x         = x;
        this.y         = y;
        this.n         = n;
        this.nn        = nn;
        this.nnn       = nnn;
    }

    int getNextOpcode() {
        return (memory.getByte(getProgramCounter()) << 8) | (memory.getByte(getProgramCounter() + 1) & 0x00FF);
    }

    void setByteInMemory(int address, int b) {
        memory.setByte(address, b);
    }

    void setIndex(int index) {
        this.index = index;
    }

    void updateIndex(IntUnaryOperator func) {
        index = func.applyAsInt(index);
    }

    void setProgramCounter(int pc) {
        this.programCounter = pc;
    }

    void updateProgramCounter(IntUnaryOperator func) {
        programCounter = func.applyAsInt(programCounter);
    }

    void pushToStack(int b) {
        stack.push(b);
    }

    int popFromStack() {
        return stack.pop();
    }

    int getDelayTimer() {
        return delayTimer;
    }

    void setDelayTimer(int delayTimer) {
        this.delayTimer = delayTimer;
    }

    void updateDelayTimer(IntUnaryOperator func) {
        delayTimer = func.applyAsInt(delayTimer);
    }

    int getSoundTimer() {
        return soundTimer;
    }

    void setSoundTimer(int soundTimer) {
        this.soundTimer = soundTimer;
    }

    void updateSoundTimer(IntUnaryOperator func) {
        soundTimer = func.applyAsInt(soundTimer);
    }

    void setDrawFlag(boolean drawFlag) {
        this.drawFlag = drawFlag;
    }

    int getRawOpcode() {
        return rawOpcode;
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    int getN() {
        return n;
    }

    int getNn() {
        return nn;
    }

    int getNnn() {
        return nnn;
    }

    void clearPixelBuffer() {
        pixelBuffer.clear();
    }

    void updatePixelInBuffer(int position, IntUnaryOperator func) {
        pixelBuffer.updatePixel(position, func);
    }

    public int getByteFromMemory(int address) {
        return memory.getByte(address);
    }

    public int getMemoryLength() {
        return memory.length;
    }

    public int getIndex() {
        return index;
    }

    public int getProgramCounter() {
        return programCounter;
    }

    public Registers getRegisters() {
        return registers;
    }

    public int[] getPixelBuffer() {
        return pixelBuffer.getPixels();
    }

    public boolean getDrawFlag() {
        return drawFlag;
    }

    private final List<Observer> observers = new LinkedList<>();

    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void notifyObservers() {
        for (var observer : observers) {
            observer.update(this);
        }
    }

    public String getDisassembledOpcode() {
        return opcode.toString();
    }
}