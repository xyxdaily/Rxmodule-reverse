package com.hepta.davlik;

import android.content.Context;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

public class JvmRuntime {

    // Used to load the 'andjvmenv' library on application startup.

    /**
     * A native method that is implemented by the 'andjvmenv' native library,
     * which is packaged with this application.
     */
    public static native ClassLoader[] getClassLoaderList();
    public static native Class<?> GlobalFindClass(String className);
    public static native void nativeLoadJvmTI();

    public static native Object[] dumpDexByCookie(long[] cookie);

    public static Class<?> FindClass( String cls){
        ClassLoader[] classLoaders = getClassLoaderList();
        for(ClassLoader classLoader : classLoaders){
            try {
                return classLoader.loadClass(cls);
            } catch (ClassNotFoundException ignored) {

            }
        }
        return null;
    }

    public static void Entry(Context context , String source,String argument){
        Log.e("JvmRuntime","23 Entry");
        LoadSo(context,source);
        nativeLoadJvmTI();
        Log.e("JvmRuntime","23 END");
    }

    public static void LoadSo(Context context, String source) {
        try {
            String abi= "arm64-v8a";
            if(!android.os.Process.is64Bit()){
                abi = "armeabi-v7a";
            }
            String str = source+"!/lib/"+abi+"/libjvmRuntime.so";
            Log.e("jvmRuntime",str);
            System.load(str);
        }catch (Exception e){
            Log.e("JvmRuntime",e.getMessage());

        }

    }


    public static void dumpdex(Context context ,String dir) {
        try {
            //TODO:to get 'pathList' field and 'dexElements' field by reflection.
            //private final DexPathList pathList;
            Class<?> baseDexClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
            Field pathListField = baseDexClassLoaderClass.getDeclaredField("pathList");
            pathListField.setAccessible(true);
            //private Element[] dexElements;
            Class<?> dexPathListClass = Class.forName("dalvik.system.DexPathList");
            Class<?> Element = Class.forName("dalvik.system.DexPathList$Element");
            Field dexElementsField = dexPathListClass.getDeclaredField("dexElements");
            Field DexFile_mCookie = DexFile.class.getDeclaredField("mCookie");
            Field DexFile_mFileName = DexFile.class.getDeclaredField("mFileName");
            Field path_filed = Element.getDeclaredField("path");
            Field dexFile_filed = Element.getDeclaredField("dexFile");
            DexFile_mCookie.setAccessible(true);
            DexFile_mFileName.setAccessible(true);
            dexElementsField.setAccessible(true);
            dexFile_filed.setAccessible(true);
            //TODO:to get the value of host's dexElements field.
            PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();
            Object BaseDexClassLoad_PathList = pathListField.get(pathClassLoader);
            Object[] DexPathList_dexElements = (Object[]) dexElementsField.get(BaseDexClassLoad_PathList);
            int i=0;
            for(Object dexElement:DexPathList_dexElements){
                DexFile dexFile = (DexFile) dexFile_filed.get(dexElement);
                //这个cookie 在android 13是一个智能指针，保存的是一个native 的 DexFile 指针
                long[] cookie = (long[]) DexFile_mCookie.get(dexFile);
                String FileName = (String) DexFile_mFileName.get(dexFile);
                Log.e("rzx","cookie = "+Long.toHexString(cookie[0])+":"+Long.toHexString(cookie[1]));
                Log.e("rzx","FileName = "+FileName);
                Object[] dex_array = dumpDexByCookie(cookie);
                for(Object dex: dex_array){
                    byte[] dex_byte = (byte[]) dex;
                    try (FileOutputStream fos = new FileOutputStream(dir+"/"+dex_byte.hashCode()+".dex")) {
                        fos.write(dex_byte); // 将字节数组写入文件
                        Log.e("rzx","dumpdex successful");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}