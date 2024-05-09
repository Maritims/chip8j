package io.github.maritims.chip8j.swing.debug;

import io.github.maritims.chip8j.Emulator;
import io.github.maritims.chip8j.Observable;
import io.github.maritims.chip8j.Observer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MemoryTable extends JTable implements Observer {
    private final DefaultTableModel model;

    public MemoryTable() {
        this.model = new DefaultTableModel(new Object[][]{}, new Object[]{
                "Address",
                "Value"
        });
        setModel(model);
        setDoubleBuffered(true);
        setFocusable(false);
    }

    public void clear() {
        model.setRowCount(0);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public void update(Observable observable) {
        var emulator = (Emulator) observable;
        if (model.getRowCount() == 0) {
            for (var i = 0; i < emulator.getCPU().getMemory().length; i++) {
                model.addRow(new Object[]{
                        String.format("%04X", i),
                        String.format("%04X", emulator.getCPU().getMemory()[i])
                });
            }
        } else {
            for (var i = 0; i < emulator.getCPU().getMemory().length; i++) {
                model.setValueAt(String.format("%04X", i), i, 0);
                model.setValueAt(String.format("%04X", emulator.getCPU().getMemory()[i]), i, 1);
            }
        }
    }
}
