package com.shutu.interview.handler;

import com.pgvector.PGvector;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@MappedTypes(List.class)
public class PGVectorTypeHandler extends BaseTypeHandler<List<Double>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Double> parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null || parameter.isEmpty()) {
            ps.setObject(i, null);
            return;
        }
        float[] floats = new float[parameter.size()];
        for (int j = 0; j < parameter.size(); j++) {
            floats[j] = parameter.get(j).floatValue();
        }
        ps.setObject(i, new PGvector(floats));
    }

    @Override
    public List<Double> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toList(rs.getObject(columnName));
    }

    @Override
    public List<Double> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toList(rs.getObject(columnIndex));
    }

    @Override
    public List<Double> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toList(cs.getObject(columnIndex));
    }

    private List<Double> toList(Object obj) throws SQLException {
        if (obj == null) {
            return null;
        }
        if (obj instanceof PGvector) {
            PGvector pgVector = (PGvector) obj;
            float[] floats = pgVector.toArray();
            List<Double> list = new ArrayList<>(floats.length);
            for (float f : floats) {
                list.add((double) f);
            }
            return list;
        }
        // Handle String representation if necessary (e.g. "[0.1, 0.2]")
        if (obj instanceof String) {
            String str = (String) obj;
            PGvector pgVector = new PGvector(str);
            float[] floats = pgVector.toArray();
            List<Double> list = new ArrayList<>(floats.length);
            for (float f : floats) {
                list.add((double) f);
            }
            return list;
        }
        return null;
    }
}
