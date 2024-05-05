package io.github.maritims.chip8j;

import io.github.maritims.chip8j.cpu.CPU;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CPUTest {
    CPU cpu;

    @BeforeEach
    void beforeEach() {
        var keypad = mock(Keypad.class);
        cpu = new CPU(keypad);
    }

    @Test
    void when_executing_0x00E0_clear_the_display() {
        // arrange
        var opcode = new CPU.Opcode((short) 0x0EE0);

        // act
        cpu.execute(opcode);

        // assert
        assertThat(cpu.getPixelBuffer()).containsOnly(0);
    }

    @Test
    void when_executing_0x00EE_set_pc_to_top_of_stack_and_decrement_sp() {
        // arrange
        var opcode = new CPU.Opcode((short) 0x00EE);
        cpu.setPC((short) 0x0000);
        cpu.getStack().push((short) 0x1234);

        // act
        cpu.execute(opcode);

        // assert
        assertThat(cpu.getPC()).isEqualTo((short) 0x1234);
        assertThat(cpu.getStack()).isEmpty();
    }

    @Test
    void when_executing_0x1000_set_pc_to_nnn() {
        // arrange
        var opcode = new CPU.Opcode((short) 0x1222);
        assertThat(opcode.nnn()).isEqualTo((short) 0x0222);
        cpu.setPC((short) 0x0000);

        // act
        cpu.execute(opcode);

        // assert
        assertThat(cpu.getPC()).isEqualTo((short) 0x0222);
    }

    @Test
    void when_executing_0x2000_push_pc_to_stack_and_set_pc_to_nnn() {
        // arrange
        var opcode = new CPU.Opcode((short) 0x2234);
        cpu.setPC((short) 0x0002);

        // act
        cpu.execute(opcode);

        // assert
        assertThat(cpu.getStack()).containsExactly((short) 0x0002);
        assertThat(cpu.getPC()).isEqualTo((short) 0x0234);
    }

    @Test
    void when_executing_0x3000_increment_pc_if_vx_equals_nn() {
        // arrange
        var opcode = new CPU.Opcode((short) 0x3511);
        assertThat(cpu.getPC()).isEqualTo((short) 0x200);
        cpu.getRegisters().set(0x0005, (byte) 0x0011);

        // act
        cpu.execute(opcode);

        // assert
        assertThat(cpu.getPC()).isEqualTo((short) 0x204);
    }

    @Test
    void when_executing_0x4000_increment_pc_if_vx_not_equals_nn() {
        // arrange
        var opcode = new CPU.Opcode((short) 0x4511);
        assertThat(cpu.getPC()).isEqualTo((short) 0x200);
        cpu.getRegisters().set(0x0006, (byte) 0x0011);

        // act
        cpu.execute(opcode);

        // assert
        assertThat(cpu.getPC()).isEqualTo((short) 0x204);
    }

    @Test
    void when_executing_0x5000_increment_pc_if_vx_equals_vy() {
        // arrange
        var opcode = new CPU.Opcode((short) 0x5540);
        assertThat(cpu.getPC()).isEqualTo((short) 0x200);
        cpu.getRegisters().set(0x0005, (byte) 0x0011);
        cpu.getRegisters().set(0x0004, (byte) 0x0011);

        // act
        cpu.execute(opcode);

        // assert
        assertThat(cpu.getPC()).isEqualTo((short) 0x204);
    }

    @Test
    void when_executing_0x6000_set_vx_to_nn() {
        // arrange
        var opcode = new CPU.Opcode((short) 0x6123);
        assertThat(cpu.getPC()).isEqualTo((short) 0x200);

        // act
        cpu.execute(opcode);

        // assert
        assertThat(cpu.getRegisters().get(0x0001)).isEqualTo((byte) 0x0023);
    }

    @Test
    void when_executing_0x7000_add_nn_to_vx() {
        // arrange
        var opcode = new CPU.Opcode((short) 0x7123);
        assertThat(cpu.getPC()).isEqualTo((short) 0x200);
        cpu.getRegisters().set(0x0001, (byte) 0x001);

        // act
        cpu.execute(opcode);

        // assert
        assertThat(cpu.getRegisters().get(0x0001)).isEqualTo((byte) 0x0024);
    }


}