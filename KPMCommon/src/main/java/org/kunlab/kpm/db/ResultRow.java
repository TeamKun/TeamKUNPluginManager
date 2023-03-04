package org.kunlab.kpm.db;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.kunlab.kpm.DebugConstants;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * {@link Transaction} で取得した {@link ResultSet} の行を表すクラスです。
 */
@SuppressWarnings("unused")
@AllArgsConstructor
public class ResultRow implements AutoCloseable
{
    private final ResultSet result;
    private final boolean closeConnectionOnException;

    private void handleException()
    {
        if (this.closeConnectionOnException)
        {
            try
            {
                this.result.getStatement().getConnection().close();
            }
            catch (SQLException e)
            {
                DebugConstants.onException(e);
            }
        }
    }

    @Override
    @SneakyThrows(SQLException.class)
    public void close()
    {
        this.result.close();
    }

    @SneakyThrows(SQLException.class)
    public void closeAll()
    {
        this.result.getStatement().getConnection().close();
    }

    public String getString(String columnLabel)
    {
        try
        {
            return this.result.getString(columnLabel);
        }
        catch (SQLException e)
        {
            this.handleException();
            throw new IllegalStateException(e);
        }
    }

    public int getInt(String columnLabel)
    {
        try
        {
            return this.result.getInt(columnLabel);
        }
        catch (SQLException e)
        {
            this.handleException();
            throw new IllegalStateException(e);
        }
    }

    public long getLong(String columnLabel)
    {
        try
        {
            return this.result.getLong(columnLabel);
        }
        catch (SQLException e)
        {
            this.handleException();
            throw new IllegalStateException(e);
        }
    }

    public float getFloat(String columnLabel)
    {
        try
        {
            return this.result.getFloat(columnLabel);
        }
        catch (SQLException e)
        {
            this.handleException();
            throw new IllegalStateException(e);
        }
    }

    public double getDouble(String columnLabel)
    {
        try
        {
            return this.result.getDouble(columnLabel);
        }
        catch (SQLException e)
        {
            this.handleException();
            throw new IllegalStateException(e);
        }
    }

    public boolean getBoolean(String columnLabel)
    {
        try
        {
            return this.result.getBoolean(columnLabel);
        }
        catch (SQLException e)
        {
            this.handleException();
            throw new IllegalStateException(e);
        }
    }

    public Date getDate(String columnLabel)
    {
        try
        {
            return this.result.getDate(columnLabel);
        }
        catch (SQLException e)
        {
            this.handleException();
            throw new IllegalStateException(e);
        }
    }

    public Time getTime(String columnLabel)
    {
        try
        {
            return this.result.getTime(columnLabel);
        }
        catch (SQLException e)
        {
            this.handleException();
            throw new IllegalStateException(e);
        }
    }

    public Timestamp getTimestamp(String columnLabel)
    {
        try
        {
            return this.result.getTimestamp(columnLabel);
        }
        catch (SQLException e)
        {
            this.handleException();
            throw new IllegalStateException(e);
        }
    }

    public Object getObject(String columnLabel)
    {
        try
        {
            return this.result.getObject(columnLabel);
        }
        catch (SQLException e)
        {
            this.handleException();
            throw new IllegalStateException(e);
        }
    }

}
