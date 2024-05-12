package io.github.maritims.chip8j.swing;

import io.github.maritims.chip8j.Emulator;
import io.github.maritims.chip8j.Observable;
import io.github.maritims.chip8j.Observer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

class OpcodeTable extends JPanel implements Observer {
    private final DefaultTableModel model;
    private final Renderer          renderer;
    private final JTable            table;

    OpcodeTable() {
        model    = new DefaultTableModel(new Object[][]{}, new Object[]{"Opcode"});
        renderer = new Renderer();
        table    = new JTable(model);

        var scrollPane = new JScrollPane(table);

        table.setDefaultRenderer(Object.class, renderer);
        table.setFocusable(false);
        table.setRowSelectionAllowed(false);
        table.setShowGrid(false);

        add(scrollPane);
    }

    void setOpcodes(List<Integer> opcodes) {
        model.setRowCount(0);
        opcodes.forEach(opcode -> model.addRow(new Object[]{String.format("%04X", opcode)}));
    }

    @Override
    public void update(Observable observable) {
        var emulator = (Emulator) observable;
        if (model.getRowCount() == 0) {
            emulator.getCPU()
                    .getOpcodes()
                    .stream()
                    .map(opcode -> String.format("%04X", opcode))
                    .map(opcode -> new String[]{opcode})
                    .forEach(model::addRow);
        }
        renderer.setCurrentOpcode(emulator.getCPU().getOpcode());
        table.repaint();
    }

    static class Renderer extends DefaultTableCellRenderer {
        private String currentOpcode;

        public void setCurrentOpcode(int currentOpcode) {
            this.currentOpcode = String.format("%04X", currentOpcode);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value == currentOpcode) {
                throw new RuntimeException("Eeek!");
                //setFont(getFont().deriveFont(Font.BOLD));
            }
            return this;
        }
    }
}