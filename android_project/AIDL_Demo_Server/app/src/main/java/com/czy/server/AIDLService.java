package com.czy.server;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：叶应是叶
 * 时间：2017/8/26 0:07
 * 描述：
 */
public class AIDLService extends Service {

    private final String TAG = "Server";

    private List<Book> bookList;

    public AIDLService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bookList = new ArrayList<>();
        initData();
        if(checkCallingPermission("android.permission.SEND_SMS")== PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(getApplicationContext(),"有权限",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"没权限",Toast.LENGTH_SHORT).show();
        }
        if(PermissionChecker.checkCallingPermission(getApplicationContext(),"android.permission.SEND_SMS","com.czy.client")==PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(getApplicationContext(),"2有权限",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"2没权限",Toast.LENGTH_SHORT).show();

        }


    }

    private void initData() {
        Book book1 = new Book("活着");
        Book book2 = new Book("或者");
        Book book3 = new Book("叶应是叶");
        Book book4 = new Book("https://github.com/leavesC");
        Book book5 = new Book("http://www.jianshu.com/u/9df45b87cfdf");
        Book book6 = new Book("http://blog.csdn.net/new_one_object");
        bookList.add(book1);
        bookList.add(book2);
        bookList.add(book3);
        bookList.add(book4);
        bookList.add(book5);
        bookList.add(book6);
    }

    private final BookController.Stub stub = new BookController.Stub() {
        @Override
        public List<Book> getBookList() throws RemoteException {
            if(checkCallingPermission("android.permission.SEND_SMS")== PackageManager.PERMISSION_GRANTED)
            {
                Log.i("xxx","有权限！");
            }
            else
            {
                Log.i("xxx","无权限！");
                //Toast.makeText(getApplicationContext(),"没权限",Toast.LENGTH_SHORT).show();
            }
            if(PermissionChecker.checkCallingPermission(getApplicationContext(),"android.permission.SEND_SMS","com.czy.client")==PackageManager.PERMISSION_GRANTED)
            {
               // Toast.makeText(getApplicationContext(),"2有权限",Toast.LENGTH_SHORT).show();
                Log.i("xxx","2有权限！");
            }
            else
            {
                Log.i("xxx","2无权限！");
                //Toast.makeText(getApplicationContext(),"2没权限",Toast.LENGTH_SHORT).show();

            }

            return bookList;
        }

        @Override
        public void addBookInOut(Book book) throws RemoteException {
            if (book != null) {
                book.setName("服务器改了新书的名字 InOut");
                bookList.add(book);
            } else {
                Log.e(TAG, "接收到了一个空对象 InOut");
            }
        }

        @Override
        public void addBookIn(Book book) throws RemoteException {

            if (book != null) {
                book.setName("服务器改了新书的名字 In");
                bookList.add(book);
            } else {
                Log.e(TAG, "接收到了一个空对象 In");
            }
        }

        @Override
        public void addBookOut(Book book) throws RemoteException {
            if (book != null) {
                Log.e(TAG, "客户端传来的书的名字：" + book.getName());
                book.setName("服务器改了新书的名字 Out");
                bookList.add(book);
            } else {
                Log.e(TAG, "接收到了一个空对象 Out");
            }
        }

    };

    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

}
