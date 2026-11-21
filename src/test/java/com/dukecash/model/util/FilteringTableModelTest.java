package com.dukecash.model.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

class FilteringTableModelTest {
    private FilteringTableModel filteringTableModel;
    private DefaultTableModel source;
    private List<TableModelEvent> events;

    @BeforeEach
    void setup() {
        events = new ArrayList<>();
        source = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int column) {
                return switch (column) {
                    case 0 -> String.class;
                    case 1 -> Boolean.class;
                    default -> super.getColumnClass(column);
                };
            }

            @SuppressWarnings({"rawtypes", "unchecked"})
            @Override
            public void setDataVector(Object[][] dataVector, Object[] columnIdentifiers) {
                this.dataVector.clear();
                for (Object[] row : dataVector) {
                    this.dataVector.add(new Vector(List.of(row)));
                }
                fireTableDataChanged();
            }
        };
        source.addColumn("Food");
        source.addColumn("Vegetable");
        source.addRow(new Object[] { "Tomato", true });
        source.addRow(new Object[] { "Carrot", false });
        source.addRow(new Object[] { "Apple", true });
        source.addRow(new Object[] { "Grape", true });

        this.filteringTableModel = new FilteringTableModel(
                source,
                sourceRow -> Boolean.TRUE.equals(source.getValueAt(sourceRow, 1)));
        filteringTableModel.addTableModelListener(events::add);
    }

    @Test
    void testInitialFiltering() {
        assertEquals(2, filteringTableModel.getColumnCount());

        assertEquals("Food", filteringTableModel.getColumnName(0));
        assertEquals("Vegetable", filteringTableModel.getColumnName(1));

        assertEquals(String.class, filteringTableModel.getColumnClass(0));
        assertEquals(Boolean.class, filteringTableModel.getColumnClass(1));

        assertEquals(3, filteringTableModel.getRowCount());

        assertEquals("Tomato", filteringTableModel.getValueAt(0, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(0, 1));
        assertEquals("Apple", filteringTableModel.getValueAt(1, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(1, 1));
        assertEquals("Grape", filteringTableModel.getValueAt(2, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(2, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> filteringTableModel.getValueAt(3, 1));
    }

    @Test
    void testDeleteFirst() {
        source.removeRow(0);

        assertEquals(2, filteringTableModel.getRowCount());

        assertEquals("Apple", filteringTableModel.getValueAt(0, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(0, 1));
        assertEquals("Grape", filteringTableModel.getValueAt(1, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(1, 1));

        assertEquals(1, events.size());
        TableModelEvent event = events.getFirst();
        assertEquals(TableModelEvent.DELETE, event.getType());
        assertEquals(0, event.getFirstRow());
        assertEquals(0, event.getLastRow());
    }

    @Test
    void testDeleteMiddle() {
        source.removeRow(2);

        assertEquals(2, filteringTableModel.getRowCount());

        assertEquals("Tomato", filteringTableModel.getValueAt(0, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(0, 1));
        assertEquals("Grape", filteringTableModel.getValueAt(1, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(1, 1));

        assertEquals(1, events.size());
        TableModelEvent event = events.getFirst();
        assertEquals(TableModelEvent.DELETE, event.getType());
        assertEquals(1, event.getFirstRow());
        assertEquals(1, event.getLastRow());
    }

    @Test
    void testDeleteLast() {
        source.removeRow(3);

        assertEquals(2, filteringTableModel.getRowCount());

        assertEquals("Tomato", filteringTableModel.getValueAt(0, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(0, 1));
        assertEquals("Apple", filteringTableModel.getValueAt(1, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(1, 1));

        assertEquals(1, events.size());
        TableModelEvent event = events.getFirst();
        assertEquals(TableModelEvent.DELETE, event.getType());
        assertEquals(2, event.getFirstRow());
        assertEquals(2, event.getLastRow());
    }

    @Test
    void testDeleteNotIncluded() {
        source.removeRow(1);

        assertEquals(3, filteringTableModel.getRowCount());

        assertEquals("Tomato", filteringTableModel.getValueAt(0, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(0, 1));
        assertEquals("Apple", filteringTableModel.getValueAt(1, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(1, 1));
        assertEquals("Grape", filteringTableModel.getValueAt(2, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(2, 1));

        assertEquals(0, events.size());
    }

    @Test
    void testInsertFirst() {
        source.insertRow(0, new Object[] { "Orange", true });

        assertEquals(4, filteringTableModel.getRowCount());

        assertEquals("Orange", filteringTableModel.getValueAt(0, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(0, 1));
        assertEquals("Tomato", filteringTableModel.getValueAt(1, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(1, 1));
        assertEquals("Apple", filteringTableModel.getValueAt(2, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(2, 1));
        assertEquals("Grape", filteringTableModel.getValueAt(3, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(3, 1));

        assertEquals(1, events.size());
        TableModelEvent event = events.getFirst();
        assertEquals(TableModelEvent.INSERT, event.getType());
        assertEquals(0, event.getFirstRow());
        assertEquals(0, event.getLastRow());
    }

    @Test
    void testInsertLast() {
        source.addRow(new Object[] { "Orange", true });

        assertEquals(4, filteringTableModel.getRowCount());

        assertEquals("Tomato", filteringTableModel.getValueAt(0, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(0, 1));
        assertEquals("Apple", filteringTableModel.getValueAt(1, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(1, 1));
        assertEquals("Grape", filteringTableModel.getValueAt(2, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(2, 1));
        assertEquals("Orange", filteringTableModel.getValueAt(3, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(3, 1));

        assertEquals(1, events.size());
        TableModelEvent event = events.getFirst();
        assertEquals(TableModelEvent.INSERT, event.getType());
        assertEquals(3, event.getFirstRow());
        assertEquals(3, event.getLastRow());
    }

    @Test
    void testInsertMiddle() {
        source.insertRow(2, new Object[] { "Orange", true });

        assertEquals(4, filteringTableModel.getRowCount());

        assertEquals("Tomato", filteringTableModel.getValueAt(0, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(0, 1));
        assertEquals("Orange", filteringTableModel.getValueAt(1, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(1, 1));
        assertEquals("Apple", filteringTableModel.getValueAt(2, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(2, 1));
        assertEquals("Grape", filteringTableModel.getValueAt(3, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(3, 1));

        assertEquals(1, events.size());
        TableModelEvent event = events.getFirst();
        assertEquals(TableModelEvent.INSERT, event.getType());
        assertEquals(1, event.getFirstRow());
        assertEquals(1, event.getLastRow());
    }

    @Test
    void testInsertNotIncluded() {
        source.insertRow(2, new Object[] { "Corn", false });

        assertEquals(3, filteringTableModel.getRowCount());

        assertEquals("Tomato", filteringTableModel.getValueAt(0, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(0, 1));
        assertEquals("Apple", filteringTableModel.getValueAt(1, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(1, 1));
        assertEquals("Grape", filteringTableModel.getValueAt(2, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(2, 1));

        assertEquals(0, events.size());
    }

    @Test
    void testUpdateRemoves() {
        source.setValueAt(false, 0, 1);

        assertEquals(2, filteringTableModel.getRowCount());

        assertEquals("Apple", filteringTableModel.getValueAt(0, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(0, 1));
        assertEquals("Grape", filteringTableModel.getValueAt(1, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(1, 1));

        assertEquals(1, events.size());
        TableModelEvent event = events.getFirst();
        assertEquals(TableModelEvent.DELETE, event.getType());
        assertEquals(0, event.getFirstRow());
        assertEquals(0, event.getLastRow());
    }

    @Test
    void testUpdateRemovesEnd() {
        source.setValueAt(false, 3, 1);

        assertEquals(2, filteringTableModel.getRowCount());

        assertEquals("Tomato", filteringTableModel.getValueAt(0, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(0, 1));
        assertEquals("Apple", filteringTableModel.getValueAt(1, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(1, 1));

        assertEquals(1, events.size());
        TableModelEvent event = events.getFirst();
        assertEquals(TableModelEvent.DELETE, event.getType());
        assertEquals(2, event.getFirstRow());
        assertEquals(2, event.getLastRow());
    }

    @Test
    void testUpdateDoesNotChangeFilterCondition() {
        source.setValueAt(true, 0, 1);

        assertEquals(3, filteringTableModel.getRowCount());

        assertEquals("Tomato", filteringTableModel.getValueAt(0, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(0, 1));
        assertEquals("Apple", filteringTableModel.getValueAt(1, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(1, 1));
        assertEquals("Grape", filteringTableModel.getValueAt(2, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(2, 1));

        assertEquals(0, events.size());
    }

    @Test
    void testUpdateAdds() {
        source.addRow(new Object[] { "Corn", false });
        events.clear();

        assertEquals(3, filteringTableModel.getRowCount());

        source.setValueAt(true, 4, 1);

        assertEquals(4, filteringTableModel.getRowCount());

        assertEquals("Tomato", filteringTableModel.getValueAt(0, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(0, 1));
        assertEquals("Apple", filteringTableModel.getValueAt(1, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(1, 1));
        assertEquals("Grape", filteringTableModel.getValueAt(2, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(2, 1));
        assertEquals("Corn", filteringTableModel.getValueAt(3, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(3, 1));

        assertEquals(1, events.size());
        TableModelEvent event = events.getFirst();
        assertEquals(TableModelEvent.INSERT, event.getType());
        assertEquals(3, event.getFirstRow());
        assertEquals(3, event.getLastRow());
    }

    @Test
    void testUpdateToEmpties() {
        source.setValueAt(false, 0, 1);
        source.setValueAt(false, 2, 1);
        source.setValueAt(false, 3, 1);

        assertEquals(0, filteringTableModel.getRowCount());

        assertEquals(3, events.size());
        assertEquals(TableModelEvent.DELETE, events.get(0).getType());
        assertEquals(0, events.get(0).getFirstRow());
        assertEquals(0, events.get(0).getLastRow());
        assertEquals(TableModelEvent.DELETE, events.get(1).getType());
        assertEquals(0, events.get(1).getFirstRow());
        assertEquals(0, events.get(1).getLastRow());
        assertEquals(TableModelEvent.DELETE, events.get(2).getType());
        assertEquals(0, events.get(2).getFirstRow());
        assertEquals(0, events.get(2).getLastRow());
    }

    @Test
    void testUpdateToNotIncluded() {
        source.setValueAt(false, 1, 1);

        assertEquals(3, filteringTableModel.getRowCount());

        assertEquals("Tomato", filteringTableModel.getValueAt(0, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(0, 1));
        assertEquals("Apple", filteringTableModel.getValueAt(1, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(1, 1));
        assertEquals("Grape", filteringTableModel.getValueAt(2, 0));
        assertEquals(Boolean.TRUE, filteringTableModel.getValueAt(2, 1));

        assertEquals(0, events.size());
    }

    @Test
    void testStructureChanged() {
        source.addColumn("Calories");

        assertEquals(3, filteringTableModel.getRowCount());

        assertEquals(1, events.size());
        TableModelEvent event = events.getFirst();
        assertEquals(TableModelEvent.UPDATE, event.getType());
        assertEquals(TableModelEvent.HEADER_ROW, event.getFirstRow());
    }

    @Test
    void testDataChanged() {
        source.setDataVector(new Object[][] {
                new Object[] { "Kiwi", true },
                new Object[] { "Orange", true },
                new Object[] { "Pineapple", true }
        }, null);

        assertEquals(3, filteringTableModel.getRowCount());

        assertEquals(1, events.size());
        TableModelEvent event = events.getFirst();
        assertEquals(TableModelEvent.UPDATE, event.getType());
        assertEquals(0, event.getFirstRow());
        assertEquals(Integer.MAX_VALUE, event.getLastRow());
    }
}