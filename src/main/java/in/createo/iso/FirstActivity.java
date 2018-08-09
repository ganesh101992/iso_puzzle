package in.createo.iso;

import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONArray;

import android.app.Activity;
import android.os.Bundle;

import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View.OnDragListener;
import android.view.DragEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnLongClickListener;
import android.graphics.drawable.Drawable;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.view.ViewGroup.LayoutParams;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.widget.TextView;


public class FirstActivity extends Activity {

    private RelativeLayout parent_layout;
    private ArrayList<ArrayList<Float>> co_ordinates = new ArrayList<ArrayList<Float>>();
    private JSONArray json;
    private ArrayList<ArrayList<Integer>> reference_matrix = new ArrayList<ArrayList<Integer>>();
    private ArrayList<ArrayList<Integer>> combinational_matrix = new ArrayList<ArrayList<Integer>>();
    private ArrayList<ArrayList<Integer>> reference_matrix_copy = new ArrayList<ArrayList<Integer>>();
    private ArrayList<ArrayList<Integer>> combinational_matrix_copy = new ArrayList<ArrayList<Integer>>();
    private ArrayList<String> edges_names = new ArrayList<>();
    private int v1, v2, json_data_size;
    private int number_of_nodes = 0, current_data = 0;
    private int node_radius, node_parent_radius, cavity_edge_height;
    private float current_x, current_y;
    private int matched = 0;
    private File storage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        //storage=new File(getExternalFilesDir("Storage"),"storage.txt");
        File dir=getApplicationContext().getDir("",Context.MODE_PRIVATE);
        storage=new File(dir, "storage.txt");
        try
        {
            FileInputStream fis=new FileInputStream(storage);
            DataInputStream dis=new DataInputStream(fis);
            BufferedReader br=new BufferedReader(new InputStreamReader(dis));
            String str,data="";
            while((str=br.readLine())!=null)
                data+=str;
            if(!data.equals("")) {
                current_data = Integer.valueOf(data);
            }
            dis.close();
        }catch(Exception e){ }

        findViewById(R.id.help_parent).setVisibility(View.GONE);
        findViewById(R.id.solution_parent).setVisibility(View.GONE);

        findViewById(R.id.button_done).setOnClickListener(new MyDoneClickListener());
        findViewById(R.id.button_done).setAlpha(0.5f);

        findViewById(R.id.button_help).setOnClickListener(new MyHelpSolutionClickListener());
        findViewById(R.id.button_help_close).setOnClickListener(new MyHelpSolutionClickListener());
        findViewById(R.id.button_solution).setOnClickListener(new MyHelpSolutionClickListener());
        findViewById(R.id.button_solution_close).setOnClickListener(new MyHelpSolutionClickListener());

        initialize_matrix();

        find_answer();

        draw_graph();
    }

    void draw_graph()
    {
        ImageView node, node_container;
        parent_layout=findViewById(R.id.parent_layout);
        node_radius=getResources().getInteger(R.integer.node_radius_size);
        node_parent_radius=getResources().getInteger(R.integer.node_parent_size);
        cavity_edge_height=getResources().getInteger(R.integer.node_edges_height);

        DisplayMetrics display= new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(display);
        int width_parent_layout=display.widthPixels;
        int height_parent_layout=display.heightPixels;
        float center_x=width_parent_layout/(float)2.0;
        float center_y=height_parent_layout/(float)2.0;
        float radius=(height_parent_layout/(float)2.0)-(node_radius);
        double current_angle=0.0;
        double inc_angle=(360/number_of_nodes);

        LinearLayout l=findViewById(R.id.solution);
        l.setY(center_y-node_radius);
        l.setX(center_x-2*node_radius);

        co_ordinates.clear();
        edges_names.clear();
        for(int i=1;i<=number_of_nodes;i++)
        {
            ArrayList<Float> temp=new ArrayList<>();
            temp.add(center_x+((float)(radius*Math.cos(Math.toRadians(current_angle)))));
            temp.add(center_y+((float)(radius*Math.sin(Math.toRadians(current_angle)))));
            co_ordinates.add(temp);
            current_angle+=inc_angle;
        }

        int count=0;
        for(int i=1;i<=number_of_nodes;i++)
            for(int j=i+1;j<=number_of_nodes;j++)
                if(reference_matrix.get(i).get(j).toString().equals("1"))
                {
                    ImageView new_node=new ImageView(FirstActivity.this);
                    LayoutParams para=new LayoutParams(40, 2*cavity_edge_height);
                    new_node.setBackground(ContextCompat.getDrawable(FirstActivity.this, R.drawable.cavity_edges));
                    String tag="cavity_edge"+count++;
                    new_node.setTag(tag);
                    new_node.setLayoutParams(para);

                    float first_x=co_ordinates.get(i-1).get(0);
                    float first_y=co_ordinates.get(i-1).get(1);
                    float second_x=co_ordinates.get(j-1).get(0);
                    float second_y=co_ordinates.get(j-1).get(1);
                    double width=Math.sqrt(Math.pow((first_y-second_y),2)+Math.pow((first_x-second_x),2));
                    new_node.getLayoutParams().width=(int)width;
                    float x=((first_x+second_x)/2)-((float)width/2);
                    new_node.setX(x);
                    float y=((first_y+second_y)/2)-cavity_edge_height;
                    new_node.setY(y);
                    y=second_y-first_y;
                    x=second_x-first_x;
                    float deg;
                    if(x!=0) {
                        if ((x < 0 && y > 0) || (x > 0 && y < 0)) {
                            deg = 180-(float) Math.toDegrees(Math.atan(Math.abs(y) / Math.abs(x)));
                        }
                        else {
                            deg = (float) Math.toDegrees(Math.atan(Math.abs(y) / Math.abs(x)));
                        }
                    }
                    else
                        deg=90;
                    new_node.setRotation(deg);

                    parent_layout.addView(new_node);
                }

        for(int i=1;i<=number_of_nodes;i++) {
            String ids = "node_container" + i;
            node_container = new ImageView(FirstActivity.this);
            LayoutParams para = new LayoutParams(2 * node_parent_radius, 2 * node_parent_radius);
            node_container.setBackground(ContextCompat.getDrawable(FirstActivity.this, R.drawable.nodes));
            node_container.setTag(ids);
            node_container.setLayoutParams(para);
            parent_layout.addView(node_container);

            float x=co_ordinates.get(i-1).get(0)-(node_parent_radius);
            node_container.setX(x);
            float y=co_ordinates.get(i-1).get(1)-(node_parent_radius);
            node_container.setY(y);
        }

        for(int i=1;i<=number_of_nodes;i++)
            for(int j=i+1;j<=number_of_nodes;j++)
                if(combinational_matrix.get(i).get(j).toString().equals("1"))
                {
                    ImageView new_node=new ImageView(FirstActivity.this);
                    LayoutParams para=new LayoutParams(40, cavity_edge_height-2);
                    new_node.setBackground(ContextCompat.getDrawable(FirstActivity.this, R.color.colorEdge));
                    String tag="edge"+i+"_"+j;
                    new_node.setTag(tag);
                    edges_names.add(tag);
                    new_node.setLayoutParams(para);

                    float first_x=co_ordinates.get(i-1).get(0);
                    float first_y=co_ordinates.get(i-1).get(1);
                    float second_x=co_ordinates.get(j-1).get(0);
                    float second_y=co_ordinates.get(j-1).get(1);
                    int diff=node_parent_radius-node_radius;
                    double width=Math.sqrt(Math.pow((first_y-second_y),2)+Math.pow((first_x-second_x),2))-(2*node_radius)+(6*diff);
                    new_node.getLayoutParams().width=(int)width;
                    float x=((first_x+second_x)/2)-((float)width/2);
                    new_node.setX(x);
                    float y=((first_y+second_y)/2)-cavity_edge_height+1+(3*diff);
                    new_node.setY(y);
                    y=second_y-first_y;
                    x=second_x-first_x;
                    float deg;
                    if(x!=0) {
                        if ((x < 0 && y > 0) || (x > 0 && y < 0)) {
                            deg = 180-(float) Math.toDegrees(Math.atan(Math.abs(y) / Math.abs(x)));
                        }
                        else {
                            deg = (float) Math.toDegrees(Math.atan(Math.abs(y) / Math.abs(x)));
                        }
                    }
                    else
                        deg=90;
                    new_node.setRotation(deg);

                    parent_layout.addView(new_node);
                }

        for(int i=1;i<=number_of_nodes;i++)
        {
            node=new ImageView(FirstActivity.this);
            LayoutParams para=new LayoutParams(2*node_radius, 2*node_radius);
            String img="node"+i;
            int img_id=getResources().getIdentifier(img, "mipmap", getPackageName());
            node.setImageResource(img_id);
            node.setLayoutParams(para);
            parent_layout.addView(node);

            String tags="node"+i;
            node.setTag(tags);
            node.setOnLongClickListener(new MyClickListener());
            node.setOnDragListener(new MyDragListener());

            float x=co_ordinates.get(i-1).get(0)-(node_radius);
            node.setX(x);
            float y=co_ordinates.get(i-1).get(1)-(node_radius);
            node.setY(y);
        }
    }

    void initialize_matrix()
    {
        String json_str = null;
        reference_matrix.clear();
        combinational_matrix.clear();
        combinational_matrix_copy.clear();
        reference_matrix_copy.clear();
        try {
            InputStream is = getAssets().open("data.json");
            int size = is.available();
            byte buffer[] = new byte[size];
            is.read(buffer);
            is.close();
            json_str = new String(buffer, "UTF-8");

            json = new JSONArray(json_str);
            json_data_size=json.length();
            number_of_nodes = json.getJSONObject(current_data).getInt("no_nodes");
            JSONArray json_arr = (json.getJSONObject(current_data).getJSONArray("reference_matrix"));
            ArrayList<Integer> header = new ArrayList<>();
            ArrayList<Integer> zeros = new ArrayList<>();
            for (int i = 0; i <= number_of_nodes; i++) {
                header.add(i);
                zeros.add(0);
            }
            reference_matrix.add((ArrayList<Integer>) header.clone());
            reference_matrix_copy.add((ArrayList<Integer>) header.clone());
            for (int i = 0; i < json_arr.length(); i++) {
                ArrayList<Integer> inner_arr = new ArrayList<>(zeros);
                inner_arr.set(0, i + 1);
                JSONArray inner_arr_json = json_arr.getJSONArray(i);
                for (int j = 0; j < inner_arr_json.length(); j++)
                    inner_arr.set(inner_arr_json.getInt(j), 1);
                reference_matrix.add((ArrayList<Integer>) inner_arr.clone());
                reference_matrix_copy.add((ArrayList<Integer>) inner_arr.clone());
            }
            json_arr = (json.getJSONObject(current_data).getJSONArray("combinational_matrix"));
            combinational_matrix.add((ArrayList<Integer>) header.clone());
            combinational_matrix_copy.add((ArrayList<Integer>) header.clone());
            for (int i = 0; i < json_arr.length(); i++) {
                ArrayList<Integer> inner_arr = new ArrayList<>(zeros);
                inner_arr.set(0, i + 1);
                JSONArray inner_arr_json = json_arr.getJSONArray(i);
                for (int j = 0; j < inner_arr_json.length(); j++)
                    inner_arr.set(inner_arr_json.getInt(j), 1);
                combinational_matrix.add((ArrayList<Integer>) inner_arr.clone());
                combinational_matrix_copy.add((ArrayList<Integer>) inner_arr.clone());
            }
        } catch (Exception e) { }
    }

    void rotate()
    {
        if(v1!=v2) {
            findViewById(R.id.button_done).setAlpha(0.5f);
            matched=0;
            int index1 = 0, index2 = 0;
            for (int i = 1; i <= number_of_nodes; i++) {
                if (combinational_matrix.get(0).get(i).toString().equals(Integer.toString(v1)))
                    index1 = i;
                else if (combinational_matrix.get(0).get(i).toString().equals(Integer.toString(v2)))
                    index2 = i;
            }
            for (int i = 0; i <= number_of_nodes; i++) {
                ArrayList<Integer> temp_arr=combinational_matrix.get(i);
                int temp = temp_arr.get(index1);
                temp_arr.set(index1,temp_arr.get(index2));
                temp_arr.set(index2, temp);
                combinational_matrix.set(i,temp_arr);
                if(i>0)
                if (index1 != i && index2 != i) {
                    //----- unable to change width and positioning error
                    if(combinational_matrix.get(i).get(index2).toString().equals("1"))
                    {
                        int vertex1=combinational_matrix.get(i).get(0);
                        int vertex2=combinational_matrix.get(0).get(index2);
                        String tag=vertex1>vertex2?("edge"+vertex2+"_"+vertex1):("edge"+vertex1+"_"+vertex2);
                        ImageView new_node=parent_layout.findViewWithTag(tag);
                        float first_x=co_ordinates.get(i-1).get(0);
                        float first_y=co_ordinates.get(i-1).get(1);
                        float second_x=co_ordinates.get(index2-1).get(0);
                        float second_y=co_ordinates.get(index2-1).get(1);
                        int diff=node_parent_radius-node_radius;
                        double width=Math.sqrt(Math.pow((first_y-second_y),2)+Math.pow((first_x-second_x),2))-(2*node_radius)+(6*diff);
                        //LayoutParams para=new LayoutParams((int)width, cavity_edge_height);
                        //new_node.setLayoutParams(para);
                        new_node.requestLayout();
                        new_node.getLayoutParams().width=(int)width;
                        new_node.getLayoutParams().height=cavity_edge_height-2;
                        new_node.setScaleType(ImageView.ScaleType.FIT_XY);
                        float x=((first_x+second_x)/2)-((float)width/2);
                        new_node.setX(x);
                        float y=((first_y+second_y)/2)-cavity_edge_height+1+(3*diff);
                        new_node.setY(y);
                        y=second_y-first_y;
                        x=second_x-first_x;
                        float deg;
                        if(x!=0) {
                            if ((x < 0 && y > 0) || (x > 0 && y < 0)) {
                                deg = 180-(float) Math.toDegrees(Math.atan(Math.abs(y) / Math.abs(x)));
                            }
                            else {
                                deg = (float) Math.toDegrees(Math.atan(Math.abs(y) / Math.abs(x)));
                            }
                        }
                        else
                            deg=90;
                        new_node.setRotation(deg);
                    }
                    if(combinational_matrix.get(i).get(index1).toString().equals("1"))
                    {
                        int vertex1=combinational_matrix.get(i).get(0);
                        int vertex2=combinational_matrix.get(0).get(index1);
                        String tag=vertex1>vertex2?("edge"+vertex2+"_"+vertex1):("edge"+vertex1+"_"+vertex2);
                        ImageView new_node=parent_layout.findViewWithTag(tag);
                        float first_x=co_ordinates.get(i-1).get(0);
                        float first_y=co_ordinates.get(i-1).get(1);
                        float second_x=co_ordinates.get(index1-1).get(0);
                        float second_y=co_ordinates.get(index1-1).get(1);
                        int diff=node_parent_radius-node_radius;
                        double width=Math.sqrt(Math.pow((first_y-second_y),2)+Math.pow((first_x-second_x),2))-(2*node_radius)+(6*diff);
                        //LayoutParams para=new LayoutParams((int)width, cavity_edge_height);
                        //new_node.setLayoutParams(para);
                        new_node.requestLayout();
                        new_node.getLayoutParams().width=(int)width;
                        new_node.getLayoutParams().height=cavity_edge_height-2;
                        new_node.setScaleType(ImageView.ScaleType.FIT_XY);
                        float x=((first_x+second_x)/2)-((float)width/2);
                        new_node.setX(x);
                        float y=((first_y+second_y)/2)-cavity_edge_height+1+(3*diff);
                        new_node.setY(y);
                        y=second_y-first_y;
                        x=second_x-first_x;
                        float deg;
                        if(x!=0) {
                            if ((x < 0 && y > 0) || (x > 0 && y < 0)) {
                                deg = 180-(float) Math.toDegrees(Math.atan(Math.abs(y) / Math.abs(x)));
                            }
                            else {
                                deg = (float) Math.toDegrees(Math.atan(Math.abs(y) / Math.abs(x)));
                            }
                        }
                        else
                            deg=90;
                        new_node.setRotation(deg);
                    }
                }
            }
            ArrayList<Integer> temp = combinational_matrix.get(index1);
            combinational_matrix.set(index1, combinational_matrix.get(index2));
            combinational_matrix.set(index2, temp);

            breakHere:for(int i=1;i<=number_of_nodes;i++) {
                for (int j = i + 1; j <= number_of_nodes; j++)
                {
                    if(reference_matrix.get(i).get(j).toString()!=combinational_matrix.get(i).get(j).toString())
                        break breakHere;
                }
                if(i==number_of_nodes)
                {
                    if(current_data<(json_data_size-1)) {
                        findViewById(R.id.button_done).setAlpha(1.0f);
                        matched = 1;
                    }
                }
            }
        }
    }

    void find_answer()
    {
        ArrayList<String> swap_history=new ArrayList<>();
        ArrayList<String> swap_history_vertex=new ArrayList<>();

        for(int i=1;i<number_of_nodes+1;i++)
        {
            ArrayList<Integer> sub_combinational_matrix=new ArrayList<>(combinational_matrix_copy.get(i).subList(1,number_of_nodes+1));
            ArrayList<Integer> sub_reference_matrix=new ArrayList<>(reference_matrix_copy.get(i).subList(1,number_of_nodes+1));
            if(!sub_combinational_matrix.toString().equals(sub_reference_matrix.toString()))
            {
                //find the row with the least mismatches
                int min_mismatch_row=0,index_min_mismatch_row=i;
                for(int j=0;j<sub_combinational_matrix.size();j++)
                {
                    if(!sub_combinational_matrix.get(j).toString().equals(sub_reference_matrix.get(j).toString()))
                        min_mismatch_row++;
                    if(min_mismatch_row>2)
                        break;
                }
                for(int j=i+1;j<number_of_nodes+1;j++) {
                    ArrayList<Integer> sub_combinational_matrix_new = new ArrayList<>(combinational_matrix_copy.get(j).subList(1, number_of_nodes + 1));
                    if (sub_combinational_matrix_new.toString().equals(sub_reference_matrix.toString()))
                    {
                        min_mismatch_row=0;
                        index_min_mismatch_row=j;
                        break;
                    }
                    else {
                        int temp_min_mismatch_row = 0;
                        for (int x = 0; x < sub_combinational_matrix_new.size(); x++) {
                            if (!sub_combinational_matrix_new.get(x).toString().equals(sub_reference_matrix.toString()))
                                temp_min_mismatch_row++;
                            if (temp_min_mismatch_row > 2)
                                break;
                        }
                        if (temp_min_mismatch_row < min_mismatch_row)
                            index_min_mismatch_row = j;
                    }
                }

                //rotate index_min_mismatch_row and i
                if(index_min_mismatch_row!=i)
                {
                    String temp=Integer.toString(i)+Integer.toString(index_min_mismatch_row);
                    swap_history.add(temp);
                    temp=Integer.toString(index_min_mismatch_row)+Integer.toString(i);
                    swap_history.add(temp);
                    temp=combinational_matrix_copy.get(0).get(i).toString()+combinational_matrix_copy.get(0).get(index_min_mismatch_row).toString();
                    swap_history_vertex.add(temp);
                    temp=combinational_matrix_copy.get(0).get(index_min_mismatch_row).toString()+combinational_matrix_copy.get(0).get(i).toString();
                    swap_history_vertex.add(temp);

                    for (int x = 0; x <= number_of_nodes; x++) {
                        ArrayList<Integer> temp_arr = combinational_matrix_copy.get(x);
                        int temp_val = temp_arr.get(i);
                        temp_arr.set(i, temp_arr.get(index_min_mismatch_row));
                        temp_arr.set(index_min_mismatch_row, temp_val);
                        combinational_matrix_copy.set(x, temp_arr);
                    }
                    ArrayList<Integer> temp_arr = combinational_matrix_copy.get(i);
                    combinational_matrix_copy.set(i, combinational_matrix_copy.get(index_min_mismatch_row));
                    combinational_matrix_copy.set(index_min_mismatch_row, temp_arr);
                }

                //find the index where there is a mismatch, in the current row i
                int swap_column_index1=0,swap_column_index2=0;
                ArrayList<Integer> sub_combinational_matrix_new=new ArrayList<>(combinational_matrix_copy.get(i).subList(1,number_of_nodes+1));
                if(!sub_combinational_matrix_new.toString().equals(sub_reference_matrix.toString()))
                {
                    for(int x=0;x<sub_combinational_matrix_new.size();x++)
                    {
                        if(!sub_combinational_matrix_new.get(x).toString().equals(sub_reference_matrix.get(x).toString()))
                        {
                            if(swap_column_index1==0)
                                swap_column_index1=x+1;
                            else
                                swap_column_index2=x+1;
                        }
                    }
                }
                if((swap_column_index1==0 || swap_column_index2==0) && (swap_column_index1!=0 || swap_column_index2!=0))
                    break;

                boolean index=false,vertex=false;
                if(swap_history.contains(Integer.toString(swap_column_index1)+Integer.toString(swap_column_index2)))
                    index=true;
                if(swap_history_vertex.contains(combinational_matrix_copy.get(0).get(swap_column_index1).toString()+combinational_matrix_copy.get(0).get(swap_column_index2).toString()))
                    vertex=true;

                //find some other row that closely matches the current row i
                boolean no_match=false;
                if(vertex && index && swap_column_index1!=0 && swap_column_index2!=0)
                {
                    swap_column_index1=i;
                    swap_column_index2=-1;
                    for(int j=i+1;j<number_of_nodes+1;j++)
                    {
                        sub_combinational_matrix_new=new ArrayList<>(combinational_matrix_copy.get(j).subList(1,number_of_nodes+1));
                        if(sub_reference_matrix.toString().equals(sub_combinational_matrix_new.toString()))
                        {
                            swap_column_index2=j;
                            break;
                        }
                        else
                        {
                            int temp_min_mismatch=0;
                            for(int x=0;x<sub_combinational_matrix_new.size();x++)
                            {
                                if(!sub_combinational_matrix_new.get(x).toString().equals(sub_reference_matrix.get(x).toString()))
                                    temp_min_mismatch++;
                                if(temp_min_mismatch>2)
                                    break;
                            }
                            if(temp_min_mismatch==2 && swap_column_index2==-1)
                            {
                                swap_column_index2=j;
                                index=false;vertex=false;
                                if(swap_history.contains(Integer.toString(swap_column_index1)+Integer.toString(swap_column_index2)))
                                    index=true;
                                if(swap_history_vertex.contains(combinational_matrix_copy.get(0).get(swap_column_index1).toString()+combinational_matrix_copy.get(0).get(swap_column_index2).toString()))
                                    vertex=true;

                                if(vertex && index)
                                    swap_column_index2=-1;
                            }
                        }
                        if(j==number_of_nodes && swap_column_index2==-1)
                            no_match=true;
                    }
                    if(no_match)
                    {
                        if(i==number_of_nodes)
                            break;
                        else
                            continue;
                    }
                }

                if(swap_column_index2==-1)
                    break;

                String temp=Integer.toString(swap_column_index1)+Integer.toString(swap_column_index2);
                swap_history.add(temp);
                temp=Integer.toString(swap_column_index2)+Integer.toString(swap_column_index1);
                swap_history.add(temp);
                temp=combinational_matrix_copy.get(0).get(swap_column_index1).toString()+combinational_matrix_copy.get(0).get(swap_column_index2).toString();
                swap_history_vertex.add(temp);
                temp=combinational_matrix_copy.get(0).get(swap_column_index2).toString()+combinational_matrix_copy.get(0).get(swap_column_index1).toString();
                swap_history_vertex.add(temp);

                for (int x = 0; x <= number_of_nodes; x++) {
                    ArrayList<Integer> temp_arr = combinational_matrix_copy.get(x);
                    int temp_val = temp_arr.get(swap_column_index1);
                    temp_arr.set(swap_column_index1, temp_arr.get(swap_column_index2));
                    temp_arr.set(swap_column_index2, temp_val);
                    combinational_matrix_copy.set(x, temp_arr);
                }
                ArrayList<Integer> temp_arr = combinational_matrix_copy.get(swap_column_index1);
                combinational_matrix_copy.set(swap_column_index1, combinational_matrix_copy.get(swap_column_index2));
                combinational_matrix_copy.set(swap_column_index2, temp_arr);

                i=0;
            }
            //if(i==number_of_nodes)
                //answer_matrix=new ArrayList<>((ArrayList<Integer>) combinational_matrix_copy.get(0).clone());
        }
    }

    private final class MyHelpSolutionClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view)
        {
            if(view==findViewById(R.id.button_help)) {
                parent_layout.setVisibility(View.GONE);
                findViewById(R.id.help_parent).setVisibility(View.VISIBLE);
            }
            else if(view==findViewById(R.id.button_help_close))
            {
                parent_layout.setVisibility(View.VISIBLE);
                findViewById(R.id.help_parent).setVisibility(View.GONE);
            }
            else if(view==findViewById(R.id.button_solution))
            {
                parent_layout.setVisibility(View.GONE);
                findViewById(R.id.solution_parent).setVisibility(View.VISIBLE);

                int s1=-1,s2=-1;
                for(int i=1;i<number_of_nodes+1;i++)
                {
                    if(!combinational_matrix.get(0).get(i).toString().equals(combinational_matrix_copy.get(0).get(i).toString()))
                    {
                        s1=combinational_matrix.get(0).get(i);
                        s2=combinational_matrix_copy.get(0).get(i);
                    }
                }

                LinearLayout r=findViewById(R.id.solution);
                r.removeAllViews();
                if(s1!=-1 && s2 !=-1) {
                    ImageView i = new ImageView(FirstActivity.this);
                    LayoutParams l = new LayoutParams(2*node_radius, 2*node_radius);
                    i.setLayoutParams(l);
                    String img = "node" + s1;
                    int img_id = getResources().getIdentifier(img, "mipmap", getPackageName());
                    i.setImageResource(img_id);
                    r.addView(i);
                    i = new ImageView(FirstActivity.this);
                    l = new LayoutParams(2*node_radius, 2*node_radius);
                    i.setLayoutParams(l);
                    img = "swap";
                    img_id = getResources().getIdentifier(img, "mipmap", getPackageName());
                    i.setImageResource(img_id);
                    r.addView(i);
                    i = new ImageView(FirstActivity.this);
                    l = new LayoutParams(2*node_radius, 2*node_radius);
                    i.setLayoutParams(l);
                    img = "node" + s2;
                    img_id = getResources().getIdentifier(img, "mipmap", getPackageName());
                    i.setImageResource(img_id);
                    r.addView(i);
                }
            }
            else if(view==findViewById(R.id.button_solution_close))
            {
                parent_layout.setVisibility(View.VISIBLE);
                findViewById(R.id.solution_parent).setVisibility(View.GONE);
            }
        }
    }

    private final class MyDoneClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view)
        {
            if(matched==1) {
                current_data++;

                try
                {
                    FileOutputStream fos=new FileOutputStream(storage);
                    fos.write(Integer.toString(current_data).getBytes());
                    fos.close();
                }catch (Exception e){}

                for (int i = 1; i <= number_of_nodes; i++) {
                    parent_layout.removeView(parent_layout.findViewWithTag("node_container" + i));
                    parent_layout.removeView(parent_layout.findViewWithTag("node" + i));
                }

                for (int i = 0; i < edges_names.size(); i++) {
                    parent_layout.removeView(parent_layout.findViewWithTag("cavity_edge" + i));
                    parent_layout.removeView(parent_layout.findViewWithTag(edges_names.get(i)));
                }

                initialize_matrix();

                find_answer();

                draw_graph();

                findViewById(R.id.button_done).setAlpha(0.5f);
                matched=0;
            }
        }
    }

    private final class MyClickListener implements OnLongClickListener
    {
        @Override
        public boolean onLongClick(View view) {
            ClipData.Item item = new ClipData.Item((CharSequence)view.getTag());
            String[] mimeTypes = { ClipDescription.MIMETYPE_TEXT_PLAIN };
            ClipData data = new ClipData(view.getTag().toString(), mimeTypes, item);
            DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            v1=Integer.parseInt(view.getTag().toString().replaceAll("[^0-9]",""));
            current_x=view.getX();
            current_y=view.getY();
            view.startDrag( data, //data to be dragged
                    shadowBuilder, //drag shadow
                    view, //local data about the drag and drop operation
                    0   //no needed flags
            );
            for(int i=0;i<edges_names.size();i++)
                if(edges_names.get(i).contains(Integer.toString(v1)))
                {
                    ImageView node1=parent_layout.findViewWithTag(edges_names.get(i));
                    node1.setAlpha(0.5f);
                }
            view.setAlpha(0.5f);
            //view.setVisibility(View.GONE);
            return true;
        }
    }

    class MyDragListener implements OnDragListener
    {
        Drawable normalShape = getResources().getDrawable(R.color.colorPrimaryDark);
        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // do nothing
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    float new_x,new_y;
                    new_x=v.getX();
                    new_y=v.getY();
                    v2=Integer.parseInt(v.getTag().toString().replaceAll("[^0-9]",""));
                    rotate();
                    v.setX(current_x);
                    v.setY(current_y);
                    current_x=new_x;
                    current_y=new_y;
                    View view = (View) event.getLocalState();
                    view.setX(current_x);
                    view.setY(current_y);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DROP:
                    // if the view is the bottomlinear, we accept the drag item
                    /*if(v == findViewById(R.id.parent_layout)) {
                        View view = (View) event.getLocalState();
                        ViewGroup viewgroup = (ViewGroup) view.getParent();
                        viewgroup.removeView(view);

                        RelativeLayout containView = (RelativeLayout) v.getParent();
                        containView.addView(view);
                        view.setVisibility(View.VISIBLE);
                    } else {
                        View view = (View) event.getLocalState();
                        view.setVisibility(View.VISIBLE);
                        Context context = getApplicationContext();
                        Toast.makeText(context, "You can't drop the image here",
                                Toast.LENGTH_LONG).show();
                        break;
                    }*/
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    view = (View) event.getLocalState();
                    for(int i=0;i<edges_names.size();i++)
                        if(edges_names.get(i).contains(Integer.toString(v1)))
                        {
                            ImageView node1=parent_layout.findViewWithTag(edges_names.get(i));
                            node1.setAlpha(1.0f);
                        }
                    view.setAlpha(1.0f);
                    //moving_node.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
            return true;
        }
    }
}