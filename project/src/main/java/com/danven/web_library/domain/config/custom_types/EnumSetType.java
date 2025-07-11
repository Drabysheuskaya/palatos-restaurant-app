package com.danven.web_library.domain.config.custom_types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Properties;

/**
 * Custom Hibernate UserType for mapping Java EnumSet to a database column.
 */
public class EnumSetType implements UserType, ParameterizedType {

    private Class<Enum> enumClass;

    /**
     * Returns the SQL types for the custom user type.
     *
     * @return an array of SQL types.
     */
    @Override
    public int[] sqlTypes() {
        return new int[]{Types.VARCHAR};
    }

    /**
     * Sets the parameters for the custom user type.
     *
     * @param parameters the properties containing the enum class name.
     */
    @Override
    public void setParameterValues(Properties parameters) {
        String enumClassName = parameters.getProperty("enumClass");
        try {
            this.enumClass = (Class<Enum>) Class.forName(enumClassName);
        } catch (ClassNotFoundException e) {
            throw new HibernateException("Enum class not found", e);
        }
    }

    /**
     * Returns the returned class of the custom user type.
     *
     * @return the returned class.
     */
    @Override
    public Class returnedClass() {
        return EnumSet.class;
    }

    /**
     * Checks if two objects are equal.
     *
     * @param x the first object.
     * @param y the second object.
     * @return true if the objects are equal, false otherwise.
     * @throws HibernateException if a Hibernate error occurs.
     */
    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return Objects.equals(x, y);
    }

    /**
     * Returns the hash code of the object.
     *
     * @param x the object.
     * @return the hash code.
     * @throws HibernateException if a Hibernate error occurs.
     */
    @Override
    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    /**
     * Retrieves the value from the result set.
     *
     * @param rs      the result set.
     * @param names   the column names.
     * @param session the session.
     * @param owner   the owner.
     * @return the retrieved value.
     * @throws HibernateException if a Hibernate error occurs.
     * @throws SQLException       if a SQL error occurs.
     */
    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
            throws HibernateException, SQLException {
        String value = rs.getString(names[0]);
        if (value == null || value.isEmpty()) {
            return EnumSet.noneOf(enumClass);
        }
        EnumSet enumSet = EnumSet.noneOf(enumClass);
        for (String name : value.split(",")) {
            enumSet.add(Enum.valueOf(enumClass, name.trim()));
        }
        return enumSet;
    }

    /**
     * Sets the value to the prepared statement.
     *
     * @param st      the prepared statement.
     * @param value   the value to set.
     * @param index   the parameter index.
     * @param session the session.
     * @throws HibernateException if a Hibernate error occurs.
     * @throws SQLException       if a SQL error occurs.
     */
    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
            throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
        } else {
            EnumSet<?> set = (EnumSet<?>) value;
            String joined = set.stream().map(Enum::name).reduce((a, b) -> a + "," + b).orElse("");
            st.setString(index, joined);
        }
    }

    /**
     * Performs a deep copy of the object.
     *
     * @param value the object to copy.
     * @return the copied object.
     * @throws HibernateException if a Hibernate error occurs.
     */
    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value instanceof EnumSet ? EnumSet.copyOf((EnumSet) value) : null;
    }

    /**
     * Checks if the custom user type is mutable.
     *
     * @return true if the custom user type is mutable, false otherwise.
     */
    @Override
    public boolean isMutable() {
        return true;
    }

    /**
     * Disassembles the object for caching.
     *
     * @param value the object to disassemble.
     * @return the disassembled object.
     * @throws HibernateException if a Hibernate error occurs.
     */
    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) deepCopy(value);
    }

    /**
     * Assembles the object from the cached representation.
     *
     * @param cached the cached representation.
     * @param owner  the owner.
     * @return the assembled object.
     * @throws HibernateException if a Hibernate error occurs.
     */
    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    /**
     * Replaces the original object with the target object.
     *
     * @param original the original object.
     * @param target   the target object.
     * @param owner    the owner.
     * @return the replaced object.
     * @throws HibernateException if a Hibernate error occurs.
     */
    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

}
