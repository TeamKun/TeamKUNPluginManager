package net.kunmc.lab.teamkunpluginmanager.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * DBのトランザクションを簡単に行うためのクラスです。
 */
public class Transaction
{
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

        config.setMaximumPoolSize(20);
        config.setLeakDetectionThreshold(300000);
        config.setAutoCommit(false);

        return new HikariDataSource(config);
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

    private boolean checkPrepareCondition()
    {
        return this.preparedStatement != null;
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
        if (!checkPrepareCondition())
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
        if (!checkPrepareCondition())
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
        if (!checkPrepareCondition())
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
        if (!checkPrepareCondition())
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
        if (!checkPrepareCondition())
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
        if (!checkPrepareCondition())
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
        if (!checkPrepareCondition())
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
        if (!checkPrepareCondition())
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
        if (!checkPrepareCondition())
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
        if (!checkPrepareCondition())
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
        if (!checkPrepareCondition())
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
                try
                {
                    this.connection.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
        }
    }

    /**
     * 更新系SQL文を実行します。
     *
     * @return 更新件数
     */
    public int executeUpdate()
    {
        return executeUpdate(true);
    }

    /**
     * クエリ系SQL文を実行します。
     *
     * @param <T> 戻り値の型
     */
    public <T> QueryResult<T> executeQuery()
    {
        if (!checkPrepareCondition())
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
            try
            {
                this.connection.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
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

        try
        {
            this.connection.close();
        }
        catch (SQLException e)
        {
            throw new IllegalStateException(e);
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
            this.connection.close();
        }
        catch (SQLException e1)
        {
            throw new IllegalStateException(e1);
        }
    }

    /**
     * DBにレコードが存在するかどうかを確認します。
     *
     * @return 存在するかどうか
     */
    public boolean isExists()
    {
        if (!checkPrepareCondition())
            throw new IllegalStateException("This TransactionHelper is not prepared.");

        try
        {
            ResultSet resultSet = this.preparedStatement.executeQuery();

            boolean result = resultSet.next();

            resultSet.close();

            return result;
        }
        catch (SQLException e)
        {
            throw new IllegalStateException(e);
        }
        finally
        {
            try
            {
                this.connection.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void close() throws Exception
    {
        if (!this.connection.isClosed())
            this.connection.close();
    }

    /**
     * クエリ更新系SQL文を実行した時に、結果を処理する関数です。
     */
    @FunctionalInterface
    public interface QueryResultConsumer
    {
        void accept(ResultSet result) throws SQLException;
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
                    catch (SQLException e1)
                    {
                        e1.printStackTrace();
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
    @AllArgsConstructor
    public class QueryResult<T>
    {
        private final ResultSet result;

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
            Transaction.this.connection.close();
        }

        /**
         * Listに変換します。
         *
         * @param resultMapper マッピング関数
         * @param max          最大件数
         * @return 変換されたList
         */
        public ArrayList<T> mapToList(Function<ResultSet, T> resultMapper, long max)
        {
            ArrayList<T> list = new ArrayList<>();

            try
            {
                while (this.result.next() && !(max == -1 || list.size() >= max))
                    list.add(resultMapper.apply(this.result));
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
        public ArrayList<T> mapToList(Function<ResultSet, T> resultMapper)
        {
            return mapToList(resultMapper, -1);
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
    }


}
