package io.github.maritims.chip8j;

import io.github.maritims.chip8j.cpu.State;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.stream.IntStream;

public class DebugDisplay extends JPanel implements Observer<State> {
    private final int               columns;
    private final int               rows;
    private final int               scale;
    private final DefaultTableModel cpuRegisterTableModel;
    private final DefaultTableModel memoryTableModel;

    public DebugDisplay(int columns, int rows, int scale) {
        this.columns               = columns;
        this.rows                  = rows;
        this.scale                 = scale;
        this.cpuRegisterTableModel = Builder.of(() -> new DefaultTableModel(null, new Object[]{"Register", "Value"}))
                .with(tableModel -> tableModel.addRow(new Object[]{"PC"}))
                .with(tableModel -> tableModel.addRow(new Object[]{"I"}))
                .with(tableModel -> IntStream.rangeClosed(0x0, 0xF).mapToObj(i -> new Object[]{"V" + Integer.toHexString(i)}).forEach(tableModel::addRow))
                .build();
        this.memoryTableModel      = Builder.of(() -> new DefaultTableModel(null, new Object[]{"Address", "Value"}))
                .build();

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setPreferredSize(new Dimension(this.columns * scale, this.rows * scale));
        setDoubleBuffered(true);

        var cpuRegisterTable = new JTable(cpuRegisterTableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        var cpuRegisterTableScrollPane = new JScrollPane(cpuRegisterTable);
        add(cpuRegisterTableScrollPane);

        var memoryTable = new JTable(memoryTableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        var memoryTableScrollPane = new JScrollPane(memoryTable);
        add(memoryTableScrollPane);
    }

    @Override
    public void update(State observable) {
        cpuRegisterTableModel.setValueAt(
                String.format("%04X", observable.getProgramCounter()) + " (" + observable.getDisassembledOpcode() + ")",
                0,
                1
        );
        cpuRegisterTableModel.setValueAt(observable.getIndex(), 1, 1);

        for (var i = 0; i < observable.getRegisters().size(); i++) {
            cpuRegisterTableModel.setValueAt(
                    observable.getRegisters().get(i),
                    i + 2,
                    1
            );
        }

        if(memoryTableModel.getRowCount() == 0) {
            for(var i = 0x200; i < observable.getMemoryLength(); i++) {
                memoryTableModel.addRow(new Object[] {
                        String.format("%04X", i),
                        String.format("%04X", observable.getByteFromMemory(i))
                });
            }
        }
    }
}
