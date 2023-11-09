package org.asf.nexus.tables;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.asf.nexus.tables.DataTable.DataTableLayout.EntryLayout;
import org.asf.nexus.tables.annotations.ForceUseFilterFields;
import org.asf.nexus.tables.annotations.TableColumn;
import org.asf.nexus.tables.annotations.UseAsFilter;

/**
 * 
 * Base data container type - table-based data system (for the common and
 * account data services)
 * 
 * @param <T> Row value type, this type is used to serialize
 * 
 * @author Sky Swimmer
 * 
 */
public abstract class DataTable<T extends TableRow> {

	private static HashMap<String, DataTableLayout> tableLayouts = new HashMap<String, DataTableLayout>();
	private DataTableLayout layout;

	protected DataTable(Class<T> cls) {
		// Find layout
		String type = cls.getTypeName();
		boolean present;
		synchronized (tableLayouts) {
			present = tableLayouts.containsKey(type);
			if (present)
				layout = tableLayouts.get(type);
		}
		if (!present) {
			// Find constructor
			Constructor<?> ctor;
			try {
				ctor = cls.getConstructor();
			} catch (NoSuchMethodException | SecurityException e) {
				throw new IllegalArgumentException("Type " + cls + " does not have any parameterless constructors");
			}

			// Create layout
			layout = new DataTableLayout(ctor, cls);

			// Populate with annotated fields
			for (Field f : cls.getFields()) {
				if (f.isAnnotationPresent(TableColumn.class)) {
					// Add
					layout.addColumn(f);
				}
			}

			// Check
			if (layout.columns.size() == 0)
				throw new IllegalArgumentException("Type " + cls
						+ " does not have any fields annotated with TableColumn, cannot create empty tables");

			// Save to memory
			synchronized (tableLayouts) {
				// Load if already present
				if (tableLayouts.containsKey(type))
					layout = tableLayouts.get(type);
				else
					tableLayouts.put(type, layout);
			}
		}
	}

	/**
	 * Checks if the table has any rows
	 * 
	 * @return True if the table has rows, false otherwise
	 * @throws IOException If the database query fails
	 */
	public boolean hasRows() throws IOException {
		return hasRows(new DataFilter());
	}

	/**
	 * Checks if there are any rows present matching the given filter
	 * 
	 * @param dataFilter Filter to use
	 * @return True if any rows match the filter, false otherwise
	 * @throws IOException If the database query fails
	 */
	public boolean hasRows(DataFilter dataFilter) throws IOException {
		verifyFilter(dataFilter);
		return hasRowsInternal(dataFilter);
	}

	/**
	 * Internal method called to check if the table has any rows matching the filter
	 * 
	 * @param dataFilter Filter to use
	 * @return True if any rows match the filter, false otherwise
	 * @throws IOException If the database query fails
	 */
	protected abstract boolean hasRowsInternal(DataFilter dataFilter) throws IOException;

	/**
	 * Retrieves the first table row from the data table
	 * 
	 * @return First table row encoded as the table layout type or null
	 * @throws IOException If the database query fails
	 */
	public T getFirstRow() throws IOException {
		return getFirstRow(new DataFilter());
	}

	/**
	 * Retrieves the first table row matching the data filter from the table
	 * 
	 * @param dataFilter Filter to use
	 * @return First table row encoded as the table layout type or null
	 * @throws IOException If the database query fails
	 */
	public T getFirstRow(DataFilter dataFilter) throws IOException {
		return dataSetToObject(getFirstRow(dataFilter, layout.getColumnNames()));
	}

	/**
	 * Retrieves the first row value of the given column name
	 * 
	 * @param <RT>       Value return type
	 * @param columnName Column name
	 * @param resultType Value return type
	 * @return Column value or null
	 * @throws IOException If the database query fails
	 */
	public <RT> RT getFirstRow(String columnName, Class<RT> resultType) throws IOException {
		return getFirstRow(new DataFilter(), columnName, resultType);
	}

	/**
	 * Retrieves the first row value of a specific column matching the given filter
	 * 
	 * @param <RT>       Value return type
	 * @param dataFilter Filter to use
	 * @param columnName Column name
	 * @param resultType Value return type
	 * @return Column value or null
	 * @throws IOException If the database query fails
	 */
	public <RT> RT getFirstRow(DataFilter dataFilter, String columnName, Class<RT> resultType) throws IOException {
		return getFirstRow(dataFilter, columnName).getValue(columnName, resultType);
	}

	/**
	 * Retrieves the first row of the table
	 * 
	 * @param firstColumnName  Name of the first column to retrieve
	 * @param otherColumnNames Names of other columns to retrieve
	 * @return Row value in the form of a DataSet instance or null if no row is
	 *         present
	 * @throws IOException If the database query fails
	 */
	public DataSet getFirstRow(String firstColumnName, String... otherColumnNames) throws IOException {
		return getFirstRow(new DataFilter(), firstColumnName, otherColumnNames);
	}

	/**
	 * Retrieves the first row of the table matching the given filter
	 * 
	 * @param dataFilter       Filter to use
	 * @param firstColumnName  Name of the first column to retrieve
	 * @param otherColumnNames Names of other columns to retrieve
	 * @return Row value in the form of a DataSet instance or null if no row is
	 *         present
	 */
	public DataSet getFirstRow(DataFilter dataFilter, String firstColumnName, String... otherColumnNames)
			throws IOException {
		ArrayList<String> columns = new ArrayList<String>();
		columns.add(firstColumnName);
		columns.addAll(Arrays.asList(otherColumnNames));
		return getFirstRow(dataFilter, columns.toArray(t -> new String[t]));
	}

	/**
	 * Internal method called to retrieve the first row of the table
	 * 
	 * @param dataFilter  Filter to use
	 * @param columnNames Column names to retrieve
	 * @throws IOException If the database query fails
	 * @return DataSet result or null if the column doesn't exist
	 * @throws IOException If the database query fails
	 */
	protected abstract DataSet getFirstRowInternal(DataFilter dataFilter, String... columnNames) throws IOException;

	/**
	 * Retrieves all rows of the table
	 * 
	 * @return Array of table row data encoded as the table layout type
	 * @throws IOException If the database query fails
	 */
	public T[] getAllRows() throws IOException {
		return getAllRows(new DataFilter());
	}

	/**
	 * Retrieves all rows of the table that are matching the filter
	 * 
	 * @param dataFilter Filter to use
	 * @return Array of table row data encoded as the table layout type
	 * @throws IOException If the database query fails
	 */
	public T[] getAllRows(DataFilter dataFilter) throws IOException {
		return dataSetsToObjects(getAllRows(dataFilter, layout.getColumnNames()));
	}

	/**
	 * Retrieves the values of the specified columns of all rows of the table
	 * 
	 * @param <RT>       Value return type
	 * @param columnName Column name
	 * @param resultType Value return type
	 * @return Array of table row values
	 * @throws IOException If the database query fails
	 */
	public <RT> RT[] getAllRows(String columnName, Class<RT> resultType) throws IOException {
		return getAllRows(new DataFilter(), columnName, resultType);
	}

	/**
	 * Retrieves the values of the specified columns of all rows matching the given
	 * filter
	 * 
	 * @param <RT>       Value return type
	 * @param dataFilter Filter to use
	 * @param columnName Column name
	 * @param resultType Value return type
	 * @return Array of table row values
	 * @throws IOException If the database query fails
	 */
	@SuppressWarnings("unchecked")
	public <RT> RT[] getAllRows(DataFilter dataFilter, String columnName, Class<RT> resultType) throws IOException {
		ArrayList<RT> res = new ArrayList<RT>();
		for (DataSet set : getAllRows(dataFilter, columnName)) {
			RT val = set.getValue(columnName, resultType);
			if (val != null)
				res.add(val);
		}
		return res.toArray(t -> (RT[]) Array.newInstance(resultType, t));
	}

	/**
	 * Retrieves the values of the specified columns of all rows in the table
	 * 
	 * @param firstColumnName  Name of the first column to retrieve
	 * @param otherColumnNames Names of other columns to retrieve
	 * @return Array of the requested values represented as DataSet instances
	 * @throws IOException If the database query fails
	 */
	public DataSet[] getAllRows(String firstColumnName, String... otherColumnNames) throws IOException {
		return getAllRows(new DataFilter(), firstColumnName, otherColumnNames);
	}

	/**
	 * Retrieves the values of the specified columns of all rows matching the given
	 * filter
	 * 
	 * @param dataFilter       Filter to use
	 * @param firstColumnName  Name of the first column to retrieve
	 * @param otherColumnNames Names of other columns to retrieve
	 * @return Array of the requested values represented as DataSet instances
	 * @throws IOException If the database query fails
	 */
	public DataSet[] getAllRows(DataFilter dataFilter, String firstColumnName, String... otherColumnNames)
			throws IOException {
		ArrayList<String> columns = new ArrayList<String>();
		columns.add(firstColumnName);
		columns.addAll(Arrays.asList(otherColumnNames));
		return getAllRows(dataFilter, columns.toArray(t -> new String[t]));
	}

	/**
	 * Internal method called to retrieve all rows of the table
	 * 
	 * @param dataFilter  Filter to use
	 * @param columnNames Column names to retrieve
	 * @throws IOException If the database query fails
	 * @return Array of DataSet instances
	 * @throws IOException If the database query fails
	 */
	protected abstract DataSet[] getAllRowsInternal(DataFilter dataFilter, String... columnNames) throws IOException;

	/**
	 * Assigns the values of all rows matching the previous values of the given
	 * object
	 * 
	 * @param value Row value
	 * @throws IOException If the database command fails
	 */
	public void setRows(T value) throws IOException {
		setRows(value, false);
	}

	/**
	 * Assigns the values of all rows matching the previous values of the given
	 * object
	 * 
	 * @param value      Row value
	 * @param replaceAll True to replace all, false to only replace rows matching
	 *                   the previous and identification values of the given row
	 * @throws IOException If the database command fails
	 */
	public void setRows(T value, boolean replaceAll) throws IOException {
		// Create update set
		DataSet set = objectToDataset(value);

		// Update
		setRows(getFilter(value, set, replaceAll), set);

		// Update value cache
		updateValueCache(value, set);
	}

	/**
	 * Assigns the values of all rows that match the given filter
	 * 
	 * @param dataFilter Filter to use
	 * @param value      Row value
	 * @throws IOException If the database command fails
	 */
	public void setRows(DataFilter dataFilter, T value) throws IOException {
		DataSet set = objectToDataset(value);
		setRows(dataFilter, set);
		updateValueCache(value, set);
	}

	/**
	 * Assigns the values of all rows that match the given filter
	 * 
	 * @param dataFilter Filter to use
	 * @param value      Row value
	 * @throws IOException If the database command fails
	 */
	public <RT> void setRows(DataFilter dataFilter, String columnName, RT value) throws IOException {
		DataSet set = new DataSet();
		set.setValue(columnName, value);
		setRows(dataFilter, set);
	}

	/**
	 * Assigns all rows of the data table that matches the given filter
	 * 
	 * @param dataFilter Filter to use
	 * @param set        Data to update
	 * @throws IOException If the database command fails
	 */
	public void setRows(DataFilter dataFilter, DataSet set) throws IOException {
		verifyFilter(dataFilter);
		verifySet(set);
		setRowsInternal(dataFilter, set);
	}

	/**
	 * Internal method called to assign all rows of the table
	 * 
	 * @param dataFilter Filter to use
	 * @param set        Data to assign
	 * @throws IOException If the database command fails
	 */
	protected abstract void setRowsInternal(DataFilter dataFilter, DataSet set) throws IOException;

	/**
	 * Removes all rows from the data table
	 * 
	 * @throws IOException If the database command fails
	 */
	public void removeRows() throws IOException {
		removeRows(new DataFilter());
	}

	/**
	 * Removes all rows from the data table that match the given row object
	 * 
	 * @param value Row to remove
	 * @throws IOException If the database command fails
	 */
	public void removeRows(T value) throws IOException {
		removeRows(getFilter(value, objectToDataset(value), false));
	}

	/**
	 * Removes all rows from the data table that match the given filter
	 * 
	 * @param dataFilter Filter to use
	 * @throws IOException If the database command fails
	 */
	public void removeRows(DataFilter dataFilter) throws IOException {
		verifyFilter(dataFilter);
		removeRowsInternal(dataFilter);
	}

	/**
	 * Internal method called to remove all rows from the data table
	 * 
	 * @param dataFilter Filter to use
	 * @throws IOException If the database command fails
	 */
	protected abstract void removeRowsInternal(DataFilter dataFilter) throws IOException;

	/**
	 * Retrieves the data table layout
	 * 
	 * @return DataTableLayout instance
	 */
	public DataTableLayout getLayout() {
		return layout;
	}

	private DataFilter getFilter(T value, DataSet set, boolean overwiteAll) {
		// Check mode
		DataFilter filter;
		if (!overwiteAll) {
			// Populate filter
			if (!getLayout().forceUseFilterFields)
				filter = new DataFilter(value.getValueCache());
			else
				filter = new DataFilter();

			// Check filter size
			if (filter.count() == 0) {
				// Populate with filter fields
				for (EntryLayout layout : getLayout().getColumns()) {
					if (layout.assignmentField != null
							&& layout.assignmentField.isAnnotationPresent(UseAsFilter.class)) {
						try {
							// Assign if not null
							Object val = layout.assignmentField.get(value);
							if (val != null) {
								filter.setValue(layout.columnName, val);
							}
						} catch (IllegalArgumentException | IllegalAccessException e) {
						}
					}
				}
			}

			// Check filter size
			if (filter.count() == 0) {
				// Still empty, populate with all values so it will create a new row
				filter = new DataFilter(objectToDataset(value));
			}
		} else
			filter = new DataFilter();

		// Remove columns that are unchanged
		String[] cols = set.getColumnNames();
		for (String col : cols) {
			Object ob = value.getValueCache().getValue(col, Object.class);
			DataType tp = value.getValueCache().getValueType(col);
			Object v = set.getValue(col, Object.class);
			if (tp != DataType.NULL) {
				if (tp != DataType.STRING && tp != DataType.OBJECT && tp != DataType.BYTE_ARRAY) {
					// Primitives
					if (ob == v) {
						// Unchanged, remove
						set.remove(col);
					}
				} else if (tp == DataType.BYTE_ARRAY) {
					// Check byte array
					byte[] o = (byte[]) ob;
					byte[] n = (byte[]) v;
					if (o == n) {
						// Initial comparison check done, both are the same initial value, so no null
						// changes
						if (n != null) {
							// New value isnt null
							if (o.length == n.length) {
								// Same length
								// Check content
								boolean same = true;
								for (int i = 0; i < o.length; i++) {
									if (o[i] != n[i]) {
										same = false;
										break;
									}
								}
								if (same) {
									// Unchanged, remove
									set.remove(col);
								}
							}
						}
					}
				} else if (tp == DataType.STRING) {
					// String
					// Check strings
					String o = (String) ob;
					String n = (String) v;
					if (o == n) {
						// Initial comparison check done, both are the same initial value, so no null
						// changes
						if (n != null) {
							// New value isnt null
							if (n.equals(o)) {
								// Unchanged, remove
								set.remove(col);
							}
						}
					}
				}
			}
		}

		// Return
		return filter;
	}

	private void updateValueCache(T value, DataSet assignmentSet) {
		value.getValueCache().clear();
		for (DataEntry ent : assignmentSet) {
			value.getValueCache().setValue(ent.getColumnName(), ent.getValue(Object.class));
		}
	}

	private DataSet getFirstRow(DataFilter filter, String[] columnNames) throws IOException {
		verifyFilter(filter);
		verifyColumns(columnNames);
		return getFirstRowInternal(filter, columnNames);
	}

	private DataSet[] getAllRows(DataFilter filter, String[] columnNames) throws IOException {
		verifyFilter(filter);
		verifyColumns(columnNames);
		return getAllRowsInternal(filter, columnNames);
	}

	private void verifyColumns(String[] columns) {
		// Go through columns
		for (String column : columns) {
			// Check column
			if (!layout.hasColumn(column))
				throw new IllegalArgumentException(
						"Request has invalid column: " + column + ": column does not exist in the table");
		}
	}

	private void verifyFilter(DataFilter filter) {
		// Go through columns
		for (DataEntry ent : filter.getValues()) {
			// Check column
			if (!layout.hasColumn(ent.getColumnName()))
				throw new IllegalArgumentException(
						"Filter has invalid column: " + ent.getColumnName() + ": column does not exist in the table");

			// Check type
			DataType expectedType = layout.getLayout(ent.getColumnName()).columnType;
			if (ent.getValueType() != DataType.NULL && ent.getValueType() != expectedType)
				throw new IllegalArgumentException("Filter has invalid value for: " + ent.getColumnName()
						+ ": value type does not match the column type, value type: " + ent.getValueType()
						+ ", expected type: " + expectedType);
		}
	}

	private void verifySet(DataSet set) {
		// Go through columns
		for (DataEntry ent : set.getValues()) {
			// Check column
			if (!layout.hasColumn(ent.getColumnName()))
				throw new IllegalArgumentException(
						"Data set has invalid column: " + ent.getColumnName() + ": column does not exist in the table");

			// Check type
			DataType expectedType = layout.getLayout(ent.getColumnName()).columnType;
			if (ent.getValueType() != DataType.NULL && ent.getValueType() != expectedType)
				throw new IllegalArgumentException("Data set has invalid value for: " + ent.getColumnName()
						+ ": value type does not match the column type, value type: " + ent.getValueType()
						+ ", expected type: " + expectedType);
		}
	}

	@SuppressWarnings("unchecked")
	private T[] dataSetsToObjects(DataSet[] sets) {
		T[] res = (T[]) Array.newInstance(layout.getType(), sets.length);
		for (int i = 0; i < res.length; i++)
			res[i] = dataSetToObject(sets[i]);
		return res;
	}

	private DataSet objectToDataset(T value) {
		// Create set
		DataSet set = new DataSet();

		// Populate
		for (EntryLayout layout : getLayout().getColumns()) {
			Object v = layout.retrievalCall.apply(value);
			if (v == null || (layout.columnType == DataType.CHAR && (int) (char) v == 0))
				continue;
			set.setValue(layout.columnName, v);
		}

		// Return
		return set;
	}

	@SuppressWarnings("unchecked")
	private T dataSetToObject(DataSet set) {
		// Check null
		if (set == null)
			return null;

		// Create instance
		T val = null;
		try {
			val = (T) layout.getObjectConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new IllegalArgumentException("Failed to create a new instance of " + layout.getType(), e);
		}

		// Clear cache
		val.getValueCache().clear();

		// Populate
		for (DataEntry entry : set) {
			// Find column
			DataTableLayout.EntryLayout column = layout.getLayout(entry.getColumnName());
			if (column == null)
				continue;

			// Verify value
			if (entry.getValueType() != DataType.NULL && column.columnType != entry.getValueType())
				throw new IllegalArgumentException("Failed to assign value for column " + column.columnName
						+ " as there was a type mismatch, database returned a " + entry.getValueType()
						+ " value but expected a " + column.columnType + " value");

			// Populate value
			column.assignmentCall.accept(entry.getValue(Object.class), val);
			val.getValueCache().setValue(entry.getColumnName(), entry.getValue(Object.class));
		}

		// Return
		return val;
	}

	public static class DataTableLayout {
		private HashMap<String, EntryLayout> columns = new HashMap<String, EntryLayout>();
		private Class<?> tableType;
		private Constructor<?> constructor;
		private boolean forceUseFilterFields;

		public DataTableLayout(Constructor<?> constr, Class<?> tableType) {
			this.tableType = tableType;
			this.constructor = constr;
			if (tableType.isAnnotationPresent(ForceUseFilterFields.class))
				forceUseFilterFields = true;
		}

		/**
		 * Checks if unspecified filters should always use filter fields
		 * 
		 * @return True if the feature is enabled, false otherwise
		 */
		public boolean forceUseFilterFields() {
			return forceUseFilterFields;
		}

		public static class EntryLayout {
			public String columnName;
			public DataType columnType;
			public Field assignmentField;
			public Class<?> objectType;
			public BiConsumer<Object, TableRow> assignmentCall;
			public Function<TableRow, Object> retrievalCall;
		}

		/**
		 * Retrieves the type of the table object
		 * 
		 * @return Class instance representing the table object type
		 */
		public Class<?> getType() {
			return tableType;
		}

		/**
		 * Retrieves the table row object empty constructor
		 * 
		 * @return Constructor instance for the type representing the table object type
		 */
		public Constructor<?> getObjectConstructor() {
			return constructor;
		}

		/**
		 * Adds columns
		 * 
		 * @param field Field to add
		 */
		public void addColumn(Field field) {
			addColumn(field.getName(), field);
		}

		/**
		 * Adds columns
		 * 
		 * @param name  Column name
		 * @param field Field instance
		 */
		public void addColumn(String name, Field field) {
			addColumn(name, DataType.fromClass(field.getType()), field);
		}

		/**
		 * Adds columns
		 * 
		 * @param name  Column name
		 * @param type  Column type
		 * @param field Field instance
		 */
		public void addColumn(String name, DataType type, Field field) {
			// Check name length
			if (name.length() > 52)
				throw new IllegalArgumentException("Column " + name + " (field " + field.getName() + ", data type "
						+ type + ") has a name that is too long, max length is 52 characters");

			// Check existence
			if (columns.containsKey(name.toUpperCase()))
				throw new IllegalArgumentException("Column " + name + " (field " + field.getName() + ", data type "
						+ type + ") was already registerd in this table"
						+ (columns.get(name.toUpperCase()).assignmentField != null ? ", existing column field: "
								+ columns.get(name.toUpperCase()).assignmentField.getName() : ""));

			// Create entry
			EntryLayout layout = new EntryLayout();
			layout.columnName = name;
			layout.columnType = type;
			layout.assignmentField = field;
			layout.objectType = field.getType();
			layout.assignmentCall = (obj, owner) -> {
				try {
					field.set(owner, obj);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new IllegalArgumentException("Failed to assign field " + field.getName() + " (column " + name
							+ ") due to an error while changing the value of the field", e);
				}
			};
			layout.retrievalCall = (owner) -> {
				try {
					return field.get(owner);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new IllegalArgumentException("Failed to retrieve the value of " + layout.columnName, e);
				}
			};
			field.setAccessible(true);
			columns.put(name.toUpperCase(), layout);
		}

		/**
		 * Adds columns
		 * 
		 * @param name             Column name
		 * @param deserializerCall Consumer called to deserialize values (receives value
		 *                         from the table)
		 * @param serializerCall   Function called to serialize values (return value is
		 *                         saved to the table)
		 * @param valueType        Value type class
		 */
		public void addColumn(String name, BiConsumer<Object, TableRow> deserializerCall,
				Function<TableRow, Object> serializerCall, Class<?> valueType) {
			// Get type
			DataType type = DataType.fromClass(valueType);

			// Check name length
			if (name.length() > 52)
				throw new IllegalArgumentException("Column " + name + " (data type " + type
						+ ") has a name that is too long, max length is 52 characters");

			// Check name
			if (!name.matches("^[0-9A-Za-z_]+$"))
				throw new IllegalArgumentException(
						"Column " + name + " (data type " + type + ") has a name that is not valid for column names");

			// Check existence
			if (columns.containsKey(name.toUpperCase()))
				throw new IllegalArgumentException(
						"Column " + name + " (data type " + type + ") was already registerd in this table"
								+ (columns.get(name.toUpperCase()).assignmentField != null ? ", existing column field: "
										+ columns.get(name.toUpperCase()).assignmentField.getName() : ""));

			// Create entry
			EntryLayout layout = new EntryLayout();
			layout.columnName = name;
			layout.columnType = type;
			layout.assignmentCall = deserializerCall;
			layout.retrievalCall = serializerCall;
			layout.objectType = valueType;
			columns.put(name.toUpperCase(), layout);
		}

		/**
		 * Retrieves all columns
		 * 
		 * @return Array of EntryLayout instances
		 */
		public EntryLayout[] getColumns() {
			return columns.values().toArray(t -> new EntryLayout[t]);
		}

		/**
		 * Retrieves all column names
		 * 
		 * @return Array of column name strings
		 */
		public String[] getColumnNames() {
			return columns.values().stream().map(t -> t.columnName).toArray(t -> new String[t]);
		}

		/**
		 * Retrieves the layout of a specific column
		 * 
		 * @param columnName Column name
		 * @return EntryLayout or null
		 */
		public EntryLayout getLayout(String columnName) {
			return columns.get(columnName.toUpperCase());
		}

		/**
		 * Checks if columns are present
		 * 
		 * @param columnName Column name
		 * @return True if present, false otherwise
		 */
		public boolean hasColumn(String columnName) {
			return columns.containsKey(columnName.toUpperCase());
		}
	}

}
