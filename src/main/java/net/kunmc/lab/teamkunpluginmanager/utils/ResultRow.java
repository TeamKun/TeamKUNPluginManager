package net.kunmc.lab.teamkunpluginmanager.utils;

import lombok.AllArgsConstructor;

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
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close()
    {
        try
        {
            this.result.close();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void closeAll()
    {
        try
        {
            this.result.getStatement().getConnection().close();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public String getString(String columnLabel)
    {
        try
        {
            return this.result.getString(columnLabel);
        }
        catch (SQLException e)
        {
            handleException();
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
            handleException();
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
            handleException();
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
            handleException();
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
            handleException();
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
            handleException();
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
            handleException();
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
            handleException();
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
            handleException();
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
            handleException();
            throw new IllegalStateException(e);
        }
    }

}
