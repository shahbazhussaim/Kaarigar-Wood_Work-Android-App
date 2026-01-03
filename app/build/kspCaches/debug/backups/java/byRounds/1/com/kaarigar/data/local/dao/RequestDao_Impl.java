package com.kaarigar.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.kaarigar.data.local.entity.RequestEntity;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class RequestDao_Impl implements RequestDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RequestEntity> __insertionAdapterOfRequestEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateStatus;

  private final SharedSQLiteStatement __preparedStmtOfClearRequests;

  public RequestDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRequestEntity = new EntityInsertionAdapter<RequestEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `requests` (`id`,`customerId`,`type`,`status`,`description`,`imageUrl`,`predictedPrice`,`workerId`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RequestEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getCustomerId());
        statement.bindString(3, entity.getType());
        statement.bindString(4, entity.getStatus());
        statement.bindString(5, entity.getDescription());
        if (entity.getImageUrl() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getImageUrl());
        }
        if (entity.getPredictedPrice() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getPredictedPrice());
        }
        if (entity.getWorkerId() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getWorkerId());
        }
        statement.bindLong(9, entity.getCreatedAt());
      }
    };
    this.__preparedStmtOfUpdateStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE requests SET status = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearRequests = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM requests";
        return _query;
      }
    };
  }

  @Override
  public Object insertRequests(final List<RequestEntity> requests,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfRequestEntity.insert(requests);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateStatus(final String requestId, final String status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateStatus.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, status);
        _argIndex = 2;
        _stmt.bindString(_argIndex, requestId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearRequests(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearRequests.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearRequests.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<RequestEntity>> getAvailableRequests() {
    final String _sql = "SELECT * FROM requests WHERE workerId IS NULL AND status = 'PENDING'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"requests"}, false, new Callable<List<RequestEntity>>() {
      @Override
      @Nullable
      public List<RequestEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customerId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfPredictedPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "predictedPrice");
          final int _cursorIndexOfWorkerId = CursorUtil.getColumnIndexOrThrow(_cursor, "workerId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<RequestEntity> _result = new ArrayList<RequestEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RequestEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpCustomerId;
            _tmpCustomerId = _cursor.getString(_cursorIndexOfCustomerId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final Double _tmpPredictedPrice;
            if (_cursor.isNull(_cursorIndexOfPredictedPrice)) {
              _tmpPredictedPrice = null;
            } else {
              _tmpPredictedPrice = _cursor.getDouble(_cursorIndexOfPredictedPrice);
            }
            final String _tmpWorkerId;
            if (_cursor.isNull(_cursorIndexOfWorkerId)) {
              _tmpWorkerId = null;
            } else {
              _tmpWorkerId = _cursor.getString(_cursorIndexOfWorkerId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new RequestEntity(_tmpId,_tmpCustomerId,_tmpType,_tmpStatus,_tmpDescription,_tmpImageUrl,_tmpPredictedPrice,_tmpWorkerId,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<RequestEntity>> getWorkerJobs(final String workerId) {
    final String _sql = "SELECT * FROM requests WHERE workerId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, workerId);
    return __db.getInvalidationTracker().createLiveData(new String[] {"requests"}, false, new Callable<List<RequestEntity>>() {
      @Override
      @Nullable
      public List<RequestEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customerId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfPredictedPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "predictedPrice");
          final int _cursorIndexOfWorkerId = CursorUtil.getColumnIndexOrThrow(_cursor, "workerId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<RequestEntity> _result = new ArrayList<RequestEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RequestEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpCustomerId;
            _tmpCustomerId = _cursor.getString(_cursorIndexOfCustomerId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final Double _tmpPredictedPrice;
            if (_cursor.isNull(_cursorIndexOfPredictedPrice)) {
              _tmpPredictedPrice = null;
            } else {
              _tmpPredictedPrice = _cursor.getDouble(_cursorIndexOfPredictedPrice);
            }
            final String _tmpWorkerId;
            if (_cursor.isNull(_cursorIndexOfWorkerId)) {
              _tmpWorkerId = null;
            } else {
              _tmpWorkerId = _cursor.getString(_cursorIndexOfWorkerId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new RequestEntity(_tmpId,_tmpCustomerId,_tmpType,_tmpStatus,_tmpDescription,_tmpImageUrl,_tmpPredictedPrice,_tmpWorkerId,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
