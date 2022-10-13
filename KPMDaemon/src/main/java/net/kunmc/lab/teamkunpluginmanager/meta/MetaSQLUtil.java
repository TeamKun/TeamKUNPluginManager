package net.kunmc.lab.teamkunpluginmanager.meta;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class MetaSQLUtil
{
    static List<String> getStringDependsOn(List<DependencyNode> nodes, DependType type)
    {
        return nodes.stream().parallel()
                .filter(node -> node.getDependType() == type)
                .map(DependencyNode::getDependsOn)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("SqlResolve")
    static void deleteAndSaveDepends(Connection connection, String tableName, String fieldName, String name,
                                     List<String> depends) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tableName + " WHERE name = ?");
        statement.setString(1, name);
        statement.executeUpdate();

        statement = connection.prepareStatement("INSERT INTO " + tableName + "(name, " + fieldName + ") VALUES(?, ?)");
        statement.setString(1, name);

        for (String depend : depends)
        {
            statement.setString(2, depend);
            statement.executeUpdate();
        }
    }

    @SuppressWarnings("SqlResolve")
    static List<String> getListFromTable(HikariDataSource source, String tableName, String name, String queryField, String field)
    {
        List<String> result = new ArrayList<>();

        try (Connection con = source.getConnection())
        {
            PreparedStatement statement =
                    con.prepareStatement("SELECT * FROM " + tableName + " WHERE " + queryField + " = ?");
            statement.setString(1, name);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next())
                result.add(resultSet.getString(field));
        }
        catch (SQLException e)
        {
            throw new IllegalStateException(e);
        }

        return result;
    }
}
