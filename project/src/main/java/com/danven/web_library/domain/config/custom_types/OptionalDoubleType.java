package com.danven.web_library.domain.config.custom_types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.DoubleType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;
import java.util.Optional;

/**
 * Custom Hibernate UserType for mapping Java Optional&lt;Double&gt; to a database column.
 */
public class OptionalDoubleType implements UserType {

    /**
     * Returns the SQL types for the custom user type.
     *
     * @return an array of SQL types.
     */
    @Override
    public int[] sqlTypes() {
        return new int[]{Types.DOUBLE};
    }

    /**
     * Returns the returned class of the custom user type.
     *
     * @return the returned class.
     */
    @Override
    public Class returnedClass() {
        return Optional.class;
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
        return x != null ? x.hashCode() : 0;
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
        Double value = (Double) DoubleType.INSTANCE.nullSafeGet(rs, names, session, owner);
        return Optional.ofNullable(value);
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
        if (value == null || !((Optional<?>) value).isPresent()) {
            st.setNull(index, Types.DOUBLE);
        } else {
            Double d = (Double) ((Optional<?>) value).get();
            DoubleType.INSTANCE.nullSafeSet(st, d, index, session);
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
        return value; // Optional is immutable but keep the type's contract
    }

    /**
     * Checks if the custom user type is mutable.
     *
     * @return true if the custom user type is mutable, false otherwise.
     */
    @Override
    public boolean isMutable() {
        return false;
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
        return (Serializable) value;
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
        return cached;
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
