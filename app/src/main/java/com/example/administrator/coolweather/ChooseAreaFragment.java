package com.example.administrator.coolweather;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.coolweather.db.City;
import com.example.administrator.coolweather.db.County;
import com.example.administrator.coolweather.db.Province;
import com.example.administrator.coolweather.util.HttpUtil;
import com.example.administrator.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static org.litepal.crud.DataSupport.findAll;

public class ChooseAreaFragment extends Fragment {
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    private int currentLevel;
    private Province selectedProvince;//选中的省
    private City selectedCity;
    private County selectedCounty;
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    private ProgressDialog progressDialog;

    /**
     * 填充布局，获取控件，适配数据
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);//填充碎片布局
        titleText = (TextView)view.findViewById(R.id.title_text);//获取控件
        backButton = (Button)view.findViewById(R.id.back_button);
        listView = (ListView)view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(MyApplication.getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //一个AdapteraView中的元素被点击时，回调一个方法
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //根据级别设置选中地区和执行下级查询，或返回上级
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);//获取点击的省份
                    queryCities();//调用方法查询该省的城市
                }else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);//获取点击的城市
                    queryCounties();//调用方法查询该城市的所有县
                }else if(currentLevel == LEVEL_COUNTY){
                    //获取天气ID，跳转到天气界面
                    //Log.d(TAG, "onItemClick: " + getActivity());
                    String weatherId = countyList.get(position).getWeatherId();
                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra("weather_id",weatherId);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();//没有点击事件时，第一次查询省份，而且初值是“中国”
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryProvinces(){
        titleText.setText("中国");//第一次进入页面赋初值“中国”
        backButton.setVisibility(View.GONE);//返回按钮不可见
        provinceList = findAll(Province.class);//取出省数据到集合list中
        if(provinceList.size()>0){//集合中有数据，取出省名称，赋值当前级别
            dataList.clear();
            for(Province province:provinceList){//遍历省集合
                dataList.add(province.getProvinceName());//取出省名称到datalist集合中
            }
            adapter.notifyDataSetChanged();//通知适配器数据发生变化，显示到界面上
            listView.setSelection(0);//指定到position[0]
            currentLevel = LEVEL_PROVINCE;//赋值级别
        }else {//集合中没有数据
            String address = "http://guolin.tech/api/china";//组装一个省数据的请求地址
            queryFromServer(address,"province");//传递省地址，从服务器查询省市县数据
        }
    }

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);//返回按钮可见
        cityList = DataSupport.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);//取出城市数据到集合list中
        if(cityList.size()>0){//集合中有数据，取出城市名称，赋值当前级别
            dataList.clear();
            for(City city:cityList){//遍历城市集合
                dataList.add(city.getCityName());//取出城市名称到datalist集合中
            }
            adapter.notifyDataSetChanged();//通知适配器数据发生变化，显示到界面上
            listView.setSelection(0);//指定到position[0]
            currentLevel = LEVEL_CITY;//赋值级别
        }else {//集合中没有数据
            int provinceCode = selectedProvince.getProvinceCode();//取出省代号
            String address = "http://guolin.tech/api/china/" + provinceCode;//组装一个城市数据的请求地址
            queryFromServer(address,"city");//传递城市请求地址，从服务器查询省市县数据
        }

    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);//返回按钮可见
        countyList = DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(County.class);//取出县数据到集合list中
        if(countyList.size()>0){//集合中有数据，取出县名称，赋值当前级别
            dataList.clear();
            for(County county:countyList){//遍历省集合
                dataList.add(county.getCountyName());//取出县名称到datalist集合中
            }
            adapter.notifyDataSetChanged();//通知适配器数据发生变化，显示到界面上
            listView.setSelection(0);//指定到position[0]
            currentLevel = LEVEL_COUNTY;//赋值级别
        }else {//集合中没有数据
            int provinceCode = selectedProvince.getProvinceCode();//取出省代号
            int cityCode = selectedCity.getCityCode();//取出市代号
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;//组装一个县数据的请求地址
            queryFromServer(address,"county");//传递县请求地址，从服务器查询省市县数据
        }

    }

    /**
     * 向服务器发送请求并返回数据进行解析并显示
     * @param address
     * @param type
     */
    private void queryFromServer(String address,final String type){
        //向服务器发送请求
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            //响应失败,切回主线程，关闭加载框，弹出加载失败提示
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(MyApplication.getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });

            }
            //响应的数据回调到onResponse中，进行数据解析，解析成功后切回主线程关闭加载框，重新调用省市县查询方法从数据库查询数据并显示出来
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();//取出响应数据
                boolean result = false;
                if("province".equals(type)){//判断是省份,解析省份的JSON数据,保存到数据库后返回真
                    result = Utility.handlerProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result = Utility.handlerCityResponse(responseText,selectedProvince.getId());
                }else if("county".equals(type)){
                    result = Utility.handlerCountyResponse(responseText,selectedCity.getId());
                }
                if(result){//如果JSON数据解析成功，切回主线程显示数据
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();//关闭加载框
                            if("province".equals(type)){
                                queryProvinces();//重新调用查询省份方法，从数据库查询数据并显示出来
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }

            }
        });
    }

    //显示进度对话框（加载框）
    private void showProgressDialog(){

        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    //关闭进度对话框
    private void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }


}
