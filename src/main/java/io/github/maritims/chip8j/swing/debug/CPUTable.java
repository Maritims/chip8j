package io.github.maritims.chip8j.swing.debug;

import io.github.maritims.chip8j.Emulator;
import io.github.maritims.chip8j.Observable;
import io.github.maritims.chip8j.Observer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CPUTable extends JTable implements Observer {
    private final DefaultTableModel model;

    public CPUTable() {
        model = new DefaultTableModel(new Object[][]{}, new Object[]{
                "Register",
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
            model.addRow(new Object[]{"PC", String.format("%04X", emulator.getCPU().getPC())});
            model.addRow(new Object[]{"I", String.format("%02X", emulator.getCPU().getI())});
            model.addRow(new Object[]{"X", String.format("%02X", emulator.getCPU().getX())});
            model.addRow(new Object[]{"Y", String.format("%02X", emulator.getCPU().getY())});

            for (var i = 0; i < emulator.getCPU().getVLength(); i++) {
                model.addRow(new Object[]{"V" + String.format("%01X", i), emulator.getCPU().getV(i)});
            }

            model.addRow(new Object[]{"DT", String.format("%01X", emulator.getCPU().getDelayTimer())});
            model.addRow(new Object[]{"ST", String.format("%01X", emulator.getCPU().getSoundTimer())});
        }
    }
}
