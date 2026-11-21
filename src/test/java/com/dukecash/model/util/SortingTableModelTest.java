package com.dukecash.model.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SortingTableModelTest {
    private SortingTableModel sortingTableModel;
    private DefaultTableModel source;
    private List<TableModelEvent> events;

    @BeforeEach
    void setup() {
        events = new ArrayList<>();
        source = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int column) {
                return String.class;
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
        source.addRow(new Object[] { "Tomato" });
        source.addRow(new Object[] { "Carrot" });
        source.addRow(new Object[] { "Apple" });
        source.addRow(new Object[] { "Grape" });

        this.sortingTableModel = new SortingTableModel(source, 0, true);
        sortingTableModel.addTableModelListener(events::add);
    }

    @Test
    void testInitialSorted() {
        assertEquals(1, sortingTableModel.getColumnCount());

        assertEquals("Food", sortingTableModel.getColumnName(0));

        assertEquals(String.class, sortingTableModel.getColumnClass(0));

        assertEquals(4, sortingTableModel.getRowCount());

        assertEquals("Apple", sortingTableModel.getValueAt(0, 0));
        assertEquals("Carrot", sortingTableModel.getValueAt(1, 0));
        assertEquals("Grape", sortingTableModel.getValueAt(2, 0));
        assertEquals("Tomato", sortingTableModel.getValueAt(3, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> sortingTableModel.getValueAt(4, 0));
    }

    @Test
    void testChangeSorting() {
        sortingTableModel.setSorting(0, false);

        assertEquals("Tomato", sortingTableModel.getValueAt(0, 0));
        assertEquals("Grape", sortingTableModel.getValueAt(1, 0));
        assertEquals("Carrot", sortingTableModel.getValueAt(2, 0));
        assertEquals("Apple", sortingTableModel.getValueAt(3, 0));

        assertEquals(1, events.size());
        TableModelEvent ev = events.getFirst();
        assertEquals(TableModelEvent.UPDATE, ev.getType());
        assertEquals(0, ev.getFirstRow());
        assertEquals(Integer.MAX_VALUE, ev.getLastRow());
    }


    @Test
    void testDeleteFirst() {
        source.removeRow(2);

        assertEquals(3, sortingTableModel.getRowCount());

        assertEquals("Carrot", sortingTableModel.getValueAt(0, 0));
        assertEquals("Grape", sortingTableModel.getValueAt(1, 0));
        assertEquals("Tomato", sortingTableModel.getValueAt(2, 0));

        assertEquals(1, events.size());
        TableModelEvent event = events.getFirst();
        assertEquals(TableModelEvent.DELETE, event.getType());
        assertEquals(0, event.getFirstRow());
        assertEquals(0, event.getLastRow());
    }

    @Test
    void testDeleteMiddle() {
        source.removeRow(1);

        assertEquals(3, sortingTableModel.getRowCount());

        assertEquals("Apple", sortingTableModel.getValueAt(0, 0));
        assertEquals("Grape", sortingTableModel.getValueAt(1, 0));
        assertEquals("Tomato", sortingTableModel.getValueAt(2, 0));

        assertEquals(1, events.size());
        TableModelEvent event = events.getFirst();
        assertEquals(TableModelEvent.DELETE, event.getType());
        assertEquals(1, event.getFirstRow());
        assertEquals(1, event.getLastRow());
    }

    @Test
    void testDeleteLast() {
        source.removeRow(0);

        assertEquals(3, sortingTableModel.getRowCount());

        assertEquals("Apple", sortingTableModel.getValueAt(0, 0));
        assertEquals("Carrot", sortingTableModel.getValueAt(1, 0));
        assertEquals("Grape", sortingTableModel.getValueAt(2, 0));

        assertEquals(1, events.size());
        TableModelEvent event = events.getFirst();
        assertEquals(TableModelEvent.DELETE, event.getType());
        assertEquals(3, event.getFirstRow());
        assertEquals(3, event.getLastRow());
    }

    @Test
    void testInsertFirst() {
        source.addRow(new Object[] { "Acai" });

        assertEquals(5, sortingTableModel.getRowCount());

        assertEquals("Acai", sortingTableModel.getValueAt(0, 0));
        assertEquals("Apple", sortingTableModel.getValueAt(1, 0));
        assertEquals("Carrot", sortingTableModel.getValueAt(2, 0));
        assertEquals("Grape", sortingTableModel.getValueAt(3, 0));
        assertEquals("Tomato", sortingTableModel.getValueAt(4, 0));

        assertEquals(1, events.size());
        TableModelEvent event = events.getFirst();
        assertEquals(TableModelEvent.INSERT, event.getType());
        assertEquals(0, event.getFirstRow());
        assertEquals(0, event.getLastRow());
    }

    @Test
    void testInsertLast() {
        source.insertRow(0, new Object[] { "Zucchini", true });

        assertEquals(5, sortingTableModel.getRowCount());

        assertEquals("Apple", sortingTableModel.getValueAt(0, 0));
        assertEquals("Carrot", sortingTableModel.getValueAt(1, 0));
        assertEquals("Grape", sortingTableModel.getValueAt(2, 0));
        assertEquals("Tomato", sortingTableModel.getValueAt(3, 0));
        assertEquals("Zucchini", sortingTableModel.getValueAt(4, 0));

        assertEquals(1, events.size());
        TableModelEvent event = events.getFirst();
        assertEquals(TableModelEvent.INSERT, event.getType());
        assertEquals(4, event.getFirstRow());
        assertEquals(4, event.getLastRow());
    }

    @Test
    void testInsertMiddle() {
        source.insertRow(2, new Object[] { "Banana", true });

        assertEquals(5, sortingTableModel.getRowCount());

        assertEquals("Apple", sortingTableModel.getValueAt(0, 0));
        assertEquals("Banana", sortingTableModel.getValueAt(1, 0));
        assertEquals("Carrot", sortingTableModel.getValueAt(2, 0));
        assertEquals("Grape", sortingTableModel.getValueAt(3, 0));
        assertEquals("Tomato", sortingTableModel.getValueAt(4, 0));

        assertEquals(1, events.size());
        TableModelEvent event = events.getFirst();
        assertEquals(TableModelEvent.INSERT, event.getType());
        assertEquals(1, event.getFirstRow());
        assertEquals(1, event.getLastRow());
    }

    @Test
    void testUpdateMovesUp() {
        source.setValueAt("Fig", 0, 0);

        assertEquals(4, sortingTableModel.getRowCount());

        assertEquals("Apple", sortingTableModel.getValueAt(0, 0));
        assertEquals("Carrot", sortingTableModel.getValueAt(1, 0));
        assertEquals("Fig", sortingTableModel.getValueAt(2, 0));
        assertEquals("Grape", sortingTableModel.getValueAt(3, 0));

        assertEquals(2, events.size());
        TableModelEvent event = events.get(0);
        assertEquals(TableModelEvent.DELETE, event.getType());
        assertEquals(3, event.getFirstRow());
        assertEquals(3, event.getLastRow());

        event = events.get(1);
        assertEquals(TableModelEvent.INSERT, event.getType());
        assertEquals(2, event.getFirstRow());
        assertEquals(2, event.getLastRow());
    }

    @Test
    void testUpdateMovesDown() {
        source.setValueAt("Durian", 2, 0);

        assertEquals(4, sortingTableModel.getRowCount());

        assertEquals("Carrot", sortingTableModel.getValueAt(0, 0));
        assertEquals("Durian", sortingTableModel.getValueAt(1, 0));
        assertEquals("Grape", sortingTableModel.getValueAt(2, 0));
        assertEquals("Tomato", sortingTableModel.getValueAt(3, 0));

        assertEquals(2, events.size());
        TableModelEvent event = events.get(0);
        assertEquals(TableModelEvent.DELETE, event.getType());
        assertEquals(0, event.getFirstRow());
        assertEquals(0, event.getLastRow());

        event = events.get(1);
        assertEquals(TableModelEvent.INSERT, event.getType());
        assertEquals(1, event.getFirstRow());
        assertEquals(1, event.getLastRow());
    }

    @Test
    void testUpdateDoesNotChangeSort() {
        source.setValueAt("Apricot", 2, 0);

        assertEquals(4, sortingTableModel.getRowCount());

        assertEquals("Apricot", sortingTableModel.getValueAt(0, 0));
        assertEquals("Carrot", sortingTableModel.getValueAt(1, 0));
        assertEquals("Grape", sortingTableModel.getValueAt(2, 0));
        assertEquals("Tomato", sortingTableModel.getValueAt(3, 0));

        assertEquals(1, events.size());
        TableModelEvent event = events.get(0);
        assertEquals(TableModelEvent.UPDATE, event.getType());
        assertEquals(0, event.getFirstRow());
        assertEquals(0, event.getLastRow());
    }

    @Test
    void testStructureChanged() {
        source.addColumn("Calories");

        assertEquals(4, sortingTableModel.getRowCount());

        assertEquals("Apple", sortingTableModel.getValueAt(0, 0));
        assertEquals("Carrot", sortingTableModel.getValueAt(1, 0));
        assertEquals("Grape", sortingTableModel.getValueAt(2, 0));
        assertEquals("Tomato", sortingTableModel.getValueAt(3, 0));

        assertEquals(1, events.size());
        TableModelEvent event = events.getFirst();
        assertEquals(TableModelEvent.UPDATE, event.getType());
        assertEquals(TableModelEvent.HEADER_ROW, event.getFirstRow());
    }



    @Test
    void testDataChanged() {
        source.setDataVector(new Object[][] {
                new Object[] { "Kiwi" },
                new Object[] { "Orange" },
                new Object[] { "Pineapple" }
        }, null);

        assertEquals(3, sortingTableModel.getRowCount());

        assertEquals(1, events.size());
        TableModelEvent event = events.getFirst();
        assertEquals(TableModelEvent.UPDATE, event.getType());
        assertEquals(0, event.getFirstRow());
        assertEquals(Integer.MAX_VALUE, event.getLastRow());
    }
}