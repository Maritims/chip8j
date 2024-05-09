package io.github.maritims.chip8j.swing;

import io.github.maritims.chip8j.CPU;
import io.github.maritims.chip8j.Emulator;
import io.github.maritims.chip8j.Observable;
import io.github.maritims.chip8j.Observer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class DebugPanel extends JPanel implements Observer {
    private final DefaultTableModel tableModel;

    public DebugPanel() {
        this.tableModel = new DefaultTableModel(new Object[][]{
        }, new Object[]{
                "Address",
                "Value"
        });

        add(new JScrollPane(new JTable(tableModel)));
        setDoubleBuffered(true);
        setVisible(true);
    }

    @Override
    public void update(Observable observable) {
        var emulator = (Emulator) observable;
        if(tableModel.getRowCount() == 0) {
            for (var i = 0; i < emulator.getCPU().getMemory().length; i++) {
                tableModel.addRow(new Object[]{i, emulator.getCPU().getMemory()[i]});
            }
        } else {
            for(var i = 0; i < emulator.getCPU().getMemory().length; i++) {
                tableModel.setValueAt(i, i, 0);
                tableModel.setValueAt(i, emulator.getCPU().getMemory()[i], 1);
            }
        }
    }
}
