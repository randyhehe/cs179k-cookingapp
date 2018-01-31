package com.example.randyhe.cookpad;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CreateRecipe extends AppCompatActivity {

    //tags
    EditText textIn;
    Button buttonAdd;
    LinearLayout container;
    int count = 0;

    //methods
    Button addMethod;
    LinearLayout methods;
    int number = 1;

    //ingredients
    Button ingAdd;
    LinearLayout ingCont;

    //publish
    Button publish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cr);

        //stuff for ingredients
        ingAdd = (Button)findViewById(R.id.adding);
        ingCont = (LinearLayout)findViewById(R.id.ing_cont);

        ingAdd.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                LayoutInflater layoutInflater =
                        (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View addView = layoutInflater.inflate(R.layout.activity_cr_row, null);

                Button ingRemove = (Button)addView.findViewById(R.id.remove);
                ingRemove.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        ((LinearLayout)addView.getParent()).removeView(addView);
                    }});

                ingCont.addView(addView);
            }});

        //stuff for tags
        textIn = (EditText)findViewById(R.id.textin);
        buttonAdd = (Button)findViewById(R.id.add);
        container = (LinearLayout)findViewById(R.id.container);

        buttonAdd.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                ++count;
                LayoutInflater layoutInflater =
                        (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View addView = layoutInflater.inflate(R.layout.activity_cr_row, null);
                TextView textOut = (TextView)addView.findViewById(R.id.textout);
                textOut.setText(textIn.getText().toString());
                textIn.setText("");
                container.setVisibility(View.VISIBLE);
                Button buttonRemove = (Button)addView.findViewById(R.id.remove);
                buttonRemove.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        --count;
                        if (count <= 0)
                        {
                            container.setVisibility(View.GONE);
                        }
                        ((LinearLayout)addView.getParent()).removeView(addView);
                    }});

                container.addView(addView);
            }});

        //stuff for methods
        addMethod = (Button)findViewById(R.id.addmethod);
        methods = (LinearLayout)findViewById(R.id.mthd_cont);

        addMethod.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                ++number;
                LayoutInflater layoutInflater =
                        (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View addView = layoutInflater.inflate(R.layout.activity_cr_method, null);
                TextView stepNum = (TextView)addView.findViewById(R.id.num);
                stepNum.setText(Integer.toString(number));


                Button method_Remove = (Button)addView.findViewById(R.id.remove_mth);
                method_Remove.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        --number;
                        ((LinearLayout)addView.getParent()).removeView(addView);
                    }});

                methods.addView(addView);
            }});

        //stuff for publish
        publish = (Button)findViewById(R.id.pub);

        publish.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                //upload to DB
            }});


    }
}
