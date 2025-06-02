package com.project.lab3simplecalculator;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private TextView resultEdit;
    private boolean justCalculated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content),(view,insets)->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //1. Result link to layout
        resultEdit = findViewById(R.id.resultEdit);
        //2. Button listeners
        int[] buttonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,
                R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7,
                R.id.btn8, R.id.btn9, R.id.btnDOT,
                R.id.btnADD, R.id.btnSUB, R.id.btnMULT, R.id.btnDIV,
                R.id.btnCE,
                R.id.btnEQ
        };
        for (int id : buttonIds) {
            findViewById(id).setOnClickListener(this::onButtonClick);
        }
    }

    // Clicking buttons
        private void onButtonClick(View view)
        {

            int id = view.getId();
            String current = resultEdit.getText().toString();
            if(justCalculated == true)
            {
                resultEdit.setText("");
                justCalculated = false;
            }
            // 1. Number or "."
            if (id == R.id.btn0 || id == R.id.btn1 || id == R.id.btn2 || id == R.id.btn3 ||
                    id == R.id.btn4 || id == R.id.btn5 || id == R.id.btn6 || id == R.id.btn7 ||
                    id == R.id.btn8 || id == R.id.btn9 || id == R.id.btnDOT) {

                // If pressed number, add together as string and put text to Textfield
                String digit = ((TextView) view).getText().toString();
                resultEdit.append(digit);
            }
            // 2. Operators
            else if (isOperator(id)) {
                // A. If empty and now digit, directly ignore
                if(TextUtils.isEmpty(current))
                {
                    return;
                }
                // B. If last character is already operator, notify
                char lastInput = current.charAt(current.length()-1);
                if(isOperator(lastInput))
                {
                    Toast.makeText(this,"Cant use two operators in a row", Toast.LENGTH_SHORT).show();
                    return;
                }
                // C. insert operator
                String op = ((TextView) view).getText().toString();
                resultEdit.append(op);
            }
            //3. CE
            else if (id == R.id.btnCE) {
                resultEdit.setText("");
            //4. Calculate
            } else if (id == R.id.btnEQ) {
                this.calculateResult(view);
                justCalculated = true;
            }

        }
        private void calculateResult(View view)
        {
            String expression = resultEdit.getText().toString();
            // A. If empty and now digit, directly ignore
            if(TextUtils.isEmpty(expression))
            {
                return;
            }
            // B. If last character is operator, notify
            char lastInput = expression.charAt(expression.length() - 1);
            if(lastInput == '+' || lastInput == '-' || lastInput == '*' || lastInput == '/')
            {
                Toast.makeText(this,"Cant use two operators in a row", Toast.LENGTH_SHORT).show();
                return;
            }
            // C. calculate result
            double result = evaluateExpression(expression);
            resultEdit.setText(String.valueOf(result));
        }
    // ==============================================================
    // C. Evaluate expression
    // - If opStack is empty, push to opStack
    // - Check operation priority of new op and top op.
    // If opStack top has higher priority, pop and pop 2 number in numSTACK
    // ==============================================================
    private double evaluateExpression(String expression) {
            // 1. PUSH INTO STACK
            expression = expression.trim();

            Stack<Character> opStack = new Stack<>();
            Stack<Double> numStack = new Stack<>();
            int i =0;
            int n = expression.length();

            while (i < n) {
                char c = expression.charAt(i);
                // If digit, read until operator
                if (Character.isDigit(c) || c == '.') {
                    StringBuilder sb = new StringBuilder();
                    while (i < n && (Character.isDigit(expression.charAt(i)) ||
                            expression.charAt(i) == '.')){
                        sb.append(expression.charAt(i));
                        i++;
                    }
                    double num = Double.parseDouble(sb.toString());
                    numStack.push(num);
                    continue;
                }
                if (c == '+' || c == '-' || c == '*' || c == '/') {
                    // If opStack is empty, push to opStack
                    if (opStack.isEmpty()) {
                        opStack.push(c);
                    } else {
                        // Check operation priority of new op and top op.
                        // If opStack top has higher priority, pop and pop 2 number in numSTACK,calculate
                        // Then push both back into stack.
                        while (!opStack.isEmpty() && (precedence(opStack.peek()) >= precedence(c))) {
                            char op = opStack.pop();
                            double num2 = numStack.pop();
                            double num1 = numStack.pop();
                            double result = opCalculate(num1,num2,op);
                            numStack.push(result);
                        }
                        // push operator if no condition apply
                        opStack.push(c);
                    }
                }
                i++;
            }

            //2. POP AND EVALUATE
            while (!opStack.isEmpty()) {
                char op = opStack.pop();
                double num2 = numStack.pop();
                double num1 = numStack.pop();
                double result = opCalculate(num1, num2, op);
                numStack.push(result);
                }
            return numStack.pop();
        }
    private int precedence(char op) {
        switch (op) {
            case '+': case '-': return 1;
            case '*': case '/': return 2;
        }
        return 0;
    }
    private double opCalculate(double num1, double num2, char op)
    {
        double result = 0.0;
        switch (op) {
            case '+':
                result = num1 + num2;
                break;
            case '-':
                result = num1 - num2;
                break;
            case '*':
                result = num1 * num2;
                break;
            case '/':
                if (num2 == 0) {
                    // If divisor is 0, throw exception
                    resultEdit.setText("");
                    String e = "Divisor = 0 is not allowed";
                    Toast.makeText(this, e, Toast.LENGTH_SHORT).show();
                    return 0;
                } else {
                    result = num1 / num2;
                }
                break;
        }
        return result;
    }

    private boolean isOperator(int id)
    {
        return id == R.id.btnADD || id == R.id.btnSUB ||
                id == R.id.btnMULT || id == R.id.btnDIV;
    }
}
