package org.kunlab.kpm.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.DebugConstants;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * DBのトランザクションを簡単に行うためのクラスです。
 */
public class Transaction implements AutoCloseable
{
    private static final int MAX_POOL_SIZE = 20;
    private static final long CONN_LEAK_DETECT_THRESHOLD = 300000L;

    /**
     * Dbのコネクションです。
     */
    @Getter
    private final Connection connection;

    @Nullable
    private final PreparedStatement preparedStatement;

    private TransactionRun beforeCommit;

    private Transaction(Connection connection, String query) throws SQLException
    {
        this.connection = connection;
        this.connection.setAutoCommit(false);

        if (query != null)
            this.preparedStatement = this.connection.prepareStatement(query);
        else
            this.preparedStatement = null;

        if (DebugConstants.DB_CONNECTION_TRACE)
            this.printCreatedOnDebug();
    }

    /**
     * トランザクションを開始します。
     *
     * @param dataSource データソース
     * @param sql        SQL文
     * @return トランザクション
     */
    public static Transaction create(@NotNull HikariDataSource dataSource, @Nullable @Language("sql") String sql)
    {
        try
        {
            return new Transaction(dataSource.getConnection(), sql);
        }
        catch (SQLException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * トランザクションを開始します。
     *
     * @param connection コネクション
     * @param sql        SQL文
     * @return トランザクション
     */
    public static Transaction create(@NotNull Connection connection, @Nullable @Language("sql") String sql)
    {
        try
        {
            return new Transaction(connection, sql);
        }
        catch (SQLException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * トランザクションを開始します。
     *
     * @param transaction 既存のトランザクション
     * @param sql         SQL文
     * @return トランザクション
     */
    public static Transaction create(@NotNull Transaction transaction, @Nullable @Language("sql") String sql)
    {
        try
        {
            return new Transaction(transaction.getConnection(), sql);
        }
        catch (SQLException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * トランザクションを開始します。
     *
     * @param dataSource データソース
     * @return トランザクション
     */
    public static Transaction create(@NotNull HikariDataSource dataSource)
    {
        return create(dataSource, null);
    }

    /**
     * SQLIte データソースを作成します。
     *
     * @param databasePath データベースのパス
     * @return データソース
     */
    public static HikariDataSource createDataSource(@NotNull Path databasePath)
    {
        HikariConfig config = new HikariConfig();

        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + databasePath);

        config.setMaximumPoolSize(MAX_POOL_SIZE);
        config.setLeakDetectionThreshold(CONN_LEAK_DETECT_THRESHOLD);
        config.setAutoCommit(false);

        return new HikariDataSource(config);
    }

    private void printCreatedOnDebug()
    {
        // Get stack and get the caller of this class
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StackTraceElement caller;
        for (int i = 0; i < stack.length; i++)
        {
            caller = stack[i];
            if (!caller.getClassName().equals(this.getClass().getName()))
                continue;

            if (i + 1 >= stack.length)
                continue;

            caller = stack[i + 1];
            if (!caller.getClassName().equals(this.getClass().getName()))
            {
                DebugConstants.debugLog("Transaction(" + this.connection.hashCode() +
                        ") created by " + caller.getClassName() + "#" + caller.getMethodName() + " at " +
                        caller.getFileName() + ":" + caller.getLineNumber());
                break;
            }
        }

    }

    /**
     * トランザクションを再生成します。
     *
     * @param newQuery 新しいSQL文
     * @return トランザクション
     */
    public Transaction renew(@Language("sql") @Nullable String newQuery)
    {
        return create(this, newQuery);
    }

    /**
     * コミット前に実行する処理を登録します。
     *
     * @param beforeCommit コミット前に実行する処理
     * @return トランザクション
     */
    public Transaction beforeCommit(TransactionRun beforeCommit)
    {
        this.beforeCommit = beforeCommit;
        return this;
    }

    private boolean isPrepared()
    {
        return this.preparedStatement == null;
    }

    /**
     * SQL文に文字列値をセットします。
     *
     * @param index インデックス
     * @param value 値
     * @return トランザクション
     */
    public Transaction set(int index, @Nullable String value)
    {
        if (this.isPrepared())
            throw new IllegalStateException("This TransactionHelper is not prepared.");

        try
        {
            this.preparedStatement.setString(index, value);
            return this;
        }
        catch (SQLException e)
        {
            throw new IllegalStateException(e);
        }

    }

    /**
     * SQL文に32ビット整数値をセットします。
     *
     * @param index インデックス
     * @param value 値
     * @return トランザクション
     */
    public Transaction set(int index, int value)
    {
        if (this.isPrepared())
            throw new IllegalStateException("This TransactionHelper is not prepared.");

        try
        {
            this.preparedStatement.setInt(index, value);
            return this;
        }
        catch (SQLException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * SQL文に真偽値をセットします。
     *
     * @param index インデックス
     * @param value 値
     * @return トランザクション
     */
    public Transaction set(int index, boolean value)
    {
        if (this.isPrepared())
            throw new IllegalStateException("This TransactionHelper is not prepared.");

        try
        {
            this.preparedStatement.setBoolean(index, value);
            return this;
        }
        catch (SQLException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * SQL文に64ビット整数値をセットします。
     *
     * @param index インデックス
     * @param value 値
     * @return トランザクション
     */
    public Transaction set(int index, long value)
    {
        if (this.isPrepared())
            throw new IllegalStateException("This TransactionHelper is not prepared.");

        try
        {
            this.preparedStatement.setLong(index, value);
            return this;
        }
        catch (SQLException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * SQL文に64ビット浮動小数点数値をセットします。
     *
     * @param index インデックス
     * @param value 値
     * @return トランザクション
     */
    public Transaction set(int index, double value)
    {
        if (this.isPrepared())
            throw new IllegalStateException("This TransactionHelper is not prepared.");

        try
        {
            this.preparedStatement.setDouble(index, value);
            return this;
        }
        catch (SQLException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * SQL文に32ビット浮動小数点数値をセットします。
     *
     * @param index インデックス
     * @param value 値
     * @return トランザクション
     */
    public Transaction set(int index, float value)
    {
        if (this.isPrepared())
            throw new IllegalStateException("This TransactionHelper is not prepared.");

        try
        {
            this.preparedStatement.setFloat(index, value);
            return this;
        }
        catch (SQLException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * SQL文にバイナリ値をセットします。
     *
     * @param index インデックス
     * @param value 値
     * @return トランザクション
     */
    public Transaction set(int index, byte value)
    {
        if (this.isPrepared())
            throw new IllegalStateException("This TransactionHelper is not prepared.");

        try
        {
            this.preparedStatement.setByte(index, value);
            return this;
        }
        catch (SQLException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * SQL文に16ビット整数値をセットします。
     *
     * @param index インデックス
     * @param value 値
     * @return トランザクション
     */
    public Transaction set(int index, short value)
    {
        if (this.isPrepared())
            throw new IllegalStateException("This TransactionHelper is not prepared.");

        try
        {
            this.preparedStatement.setShort(index, value);
            return this;
        }
        catch (SQLException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * SQL文にバイト配列をセットします。
     *
     * @param index インデックス
     * @param value 値
     * @return トランザクション
     */
    public Transaction set(int index, byte[] value)
    {
        if (this.isPrepared())
            throw new IllegalStateException("This TransactionHelper is not prepared.");

        try
        {
            this.preparedStatement.setBytes(index, value);
            return this;
        }
        catch (SQLException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * SQL文にNULL値をセットします。
     *
     * @param index インデックス
     * @param type  型
     * @return トランザクション
     */
    public Transaction setNull(int index, int type)
    {
        if (this.isPrepared())
            throw new IllegalStateException("This TransactionHelper is not prepared.");

        try
        {
            this.preparedStatement.setNull(index, type);
            return this;
        }
        catch (SQLException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 更新系SQL文を実行します。
     *
     * @param autoFinish 自動終了するかどうか
     * @return 更新件数
     */
    public int executeUpdate(boolean autoFinish)
    {
        if (this.isPrepared())
            throw new IllegalStateException("This TransactionHelper is not prepared.");

        try
        {
            int result = this.preparedStatement.executeUpdate();

            if (autoFinish)
            {
                if (this.beforeCommit != null)
                    this.beforeCommit.run(this);

                this.connection.commit();
            }

            return result;
        }
        catch (SQLException e)
        {
            try
            {
                this.connection.rollback();
            }
            catch (SQLException e1)
            {
                throw new IllegalStateException(e1);
            }

            throw new IllegalStateException(e);
        }
        finally
        {

            if (autoFinish)
                this.close();
        }
    }

    /**
     * 更新系SQL文を実行します。
     *
     * @return 更新件数
     */
    public int executeUpdate()
    {
        return this.executeUpdate(true);
    }

    /**
     * クエリ系SQL文を実行します。
     *
     * @param <T> 戻り値の型
     */
    public <T> QueryResult<T> executeQuery()
    {
        if (this.isPrepared())
            throw new IllegalStateException("This TransactionHelper is not prepared.");

        try
        {
            ResultSet resultSet = this.preparedStatement.executeQuery();

            return new QueryResult<>(resultSet);
        }
        catch (SQLException e)
        {
            try
            {
                this.connection.rollback();
                this.connection.close();
            }
            catch (SQLException e1)
            {
                throw new IllegalStateException(e1);
            }

            throw new IllegalStateException(e);
        }
    }

    /**
     * トランザクションを実行し、コミットします。
     *
     * @param transactionRun トランザクションを処理する関数
     */
    public void doTransaction(TransactionRun transactionRun)
    {
        try
        {
            transactionRun.run(this);

            if (this.beforeCommit != null)
                this.beforeCommit.run(this);
            this.connection.commit();
        }
        catch (SQLException e)
        {
            try
            {
                this.connection.rollback();
            }
            catch (SQLException e1)
            {
                throw new IllegalStateException(e1);
            }

            throw new IllegalStateException(e);
        }
        finally
        {
            this.close();
        }
    }

    /**
     * 手動でトランザクションをコミットし終了します。
     */
    public void finishManually()
    {
        try
        {
            if (this.beforeCommit != null)
                this.beforeCommit.run(this);
            this.connection.commit();
        }
        catch (SQLException e)
        {
            try
            {
                this.connection.rollback();
            }
            catch (SQLException e1)
            {
                throw new IllegalStateException(e1);
            }

            throw new IllegalStateException(e);
        }
        finally
        {
            this.close();
        }
    }

    /**
     * 手動でトランザクションをロールバックし終了します。
     */
    public void abortManually()
    {
        try
        {
            this.connection.rollback();
        }
        catch (SQLException e1)
        {
            throw new IllegalStateException(e1);
        }

        this.close();
    }

    /**
     * DBにレコードが存在するかどうかを確認します。
     *
     * @param closeConnection 終了時にコネクションを閉じるかどうか
     * @return 存在するかどうか
     */
    public boolean isExists(boolean closeConnection)
    {
        if (this.isPrepared())
            throw new IllegalStateException("This TransactionHelper is not prepared.");

        try
        {
            ResultSet resultSet = this.preparedStatement.executeQuery();

            boolean result = resultSet.next();

            resultSet.close();
            if (closeConnection)
                this.close();

            return result;
        }
        catch (SQLException e)
        {
            throw new IllegalStateException(e);
        }
        finally
        {
            this.close();
        }
    }

    /**
     * DBにレコードが存在するかどうかを確認します。
     *
     * @return 存在するかどうか
     */
    public boolean isExists()
    {
        return this.isExists(true);
    }

    @Override
    public void close()
    {
        try
        {
            if (!this.connection.isClosed())
                this.connection.close();

            DebugConstants.debugLog(
                    "Transaction(" + this.connection.hashCode() + ") is closed.",
                    DebugConstants.DB_CONNECTION_TRACE
            );
        }
        catch (SQLException ignored)
        {
        }
    }

    /**
     * トランザクションを実行する汎用関数です。
     */
    @FunctionalInterface
    public interface TransactionRun
    {
        void run(Transaction transaction) throws SQLException;
    }

    private static class QueryResultSpliterator implements Spliterator<ResultRow>
    {
        private final ResultSet result;
        private final boolean closeConnectionOnException;

        public QueryResultSpliterator(ResultSet result, boolean closeConnectionOnException)
        {
            this.result = result;
            this.closeConnectionOnException = closeConnectionOnException;
        }

        @Override
        public boolean tryAdvance(Consumer<? super ResultRow> action)
        {
            try
            {
                if (this.result.next())
                {
                    action.accept(new ResultRow(this.result, this.closeConnectionOnException));
                    return true;
                }
                else
                    return false;
            }
            catch (SQLException e)
            {
                if (this.closeConnectionOnException)
                {
                    try
                    {
                        this.result.getStatement().getConnection().close();
                    }
                    catch (SQLException ignored)
                    {
                    }
                }
                throw new IllegalStateException(e);
            }
        }

        @Override
        public Spliterator<ResultRow> trySplit()
        {
            return null;
        }

        @Override
        public long estimateSize()
        {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics()
        {
            return ORDERED;
        }
    }

    /**
     * クエリの実行結果を表すクラスです。
     */
    @RequiredArgsConstructor
    public static class QueryResult<T>
    {
        private final ResultSet result;
        @Setter
        @Accessors(chain = true)
        private Function<ResultRow, T> mapper;

        /**
         * ResultSetをそのまま取得します。
         *
         * @return ResultSet
         */
        public ResultSet getResult()
        {
            return this.result;
        }

        /**
         * この結果を解放します。
         */
        public void close() throws SQLException
        {
            this.result.close();
        }

        /**
         * Listに変換します。
         *
         * @param resultMapper マッピング関数
         * @param max          最大件数
         * @return 変換されたList
         */
        public List<T> mapToList(Function<? super ResultRow, ? extends T> resultMapper, long max)
        {
            List<T> list = new ArrayList<>();

            try
            {
                while (this.result.next() && !(max == -1 || list.size() >= max))
                    list.add(resultMapper.apply(new ResultRow(this.result, true)));
            }
            catch (SQLException e)
            {
                throw new IllegalStateException(e);
            }

            return list;
        }

        /**
         * Listに変換します。
         *
         * @param resultMapper マッピング関数
         * @return 変換されたList
         */
        public List<T> mapToList(Function<ResultRow, T> resultMapper)
        {
            return this.mapToList(resultMapper, -1);
        }

        /**
         * Streamに変換します。
         *
         * @return 変換されたStream
         */
        public Stream<ResultRow> stream(boolean closeConnectionOnException)
        {
            return StreamSupport.stream(
                    new QueryResultSpliterator(this.result, closeConnectionOnException),
                    false
            );
        }

        /**
         * Streamに変換します。
         *
         * @return 変換されたStream
         */
        public Stream<ResultRow> stream()
        {
            return this.stream(true);
        }

        /**
         * 次の行に移動します。
         *
         * @return 次の行が存在するかどうか
         */
        public boolean next()
        {
            try
            {
                return this.result.next();
            }
            catch (SQLException e)
            {
                throw new IllegalStateException(e);
            }
        }

        /**
         * 行の内容をマップして返します。
         *
         * @return マップされた行の内容
         */
        public T get()
        {
            if (this.mapper == null)
                throw new IllegalStateException("Mapper is not set.");

            return this.mapper.apply(new ResultRow(this.result, true));
        }
    }


}
