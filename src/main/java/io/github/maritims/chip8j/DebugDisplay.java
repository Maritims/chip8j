package io.github.maritims.chip8j;

import io.github.maritims.chip8j.cpu.CPU;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.stream.IntStream;

public class DebugDisplay extends JPanel implements Observer {
    private final DefaultTableModel cpuRegisterTableModel;
    private final DefaultTableModel memoryTableModel;

    public DebugDisplay(int columns, int rows, int scale) {
        this.cpuRegisterTableModel = Builder.of(() -> new DefaultTableModel(null, new Object[]{"Register", "Value"}))
                .with(tableModel -> tableModel.addRow(new Object[]{"PC"}))
                .with(tableModel -> tableModel.addRow(new Object[]{"I"}))
                .with(tableModel -> IntStream.rangeClosed(0x0, 0xF).mapToObj(i -> new Object[]{"V" + Integer.toHexString(i)}).forEach(tableModel::addRow))
                .build();
        this.memoryTableModel      = Builder.of(() -> new DefaultTableModel(null, new Object[]{"Address", "Value"}))
                .build();

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setPreferredSize(new Dimension(columns * scale, rows * scale));
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
    public void update(Observable observable) {
        var cpu = (CPU) observable;

        cpuRegisterTableModel.setValueAt(
                String.format("%04X", cpu.getPC()) + " ()",
                0,
                1
        );
        cpuRegisterTableModel.setValueAt(cpu.getI(), 1, 1);

        for (var i = 0; i < cpu.getVLength(); i++) {
            cpuRegisterTableModel.setValueAt(
                    cpu.getV(i),
                    i + 2,
                    1
            );
        }

        if(memoryTableModel.getRowCount() == 0) {
            for(var i = 0x200; i < cpu.getMemory().length; i++) {
                memoryTableModel.addRow(new Object[] {
                        String.format("%04X", i),
                        String.format("%04X", cpu.getMemory()[i])
                });
            }
        }
    }
}
