package com.springmvc.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springmvc.annotation.Controller;
import com.springmvc.annotation.RequestMapping;
import com.springmvc.annotation.RequestParm;
import com.springmvc.annotation.ResponseBody;
import com.springmvc.context.WebApplicationContext;
import com.springmvc.exception.ContextException;
import com.springmvc.handler.MyHandler;
import com.sun.xml.internal.ws.api.model.MEP;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {

    private WebApplicationContext webApplicationContext;

    //存储url和对象方法映射关系
    List<MyHandler> myHandlerList = new ArrayList<MyHandler>();

    @Override
    public void init() throws ServletException {
        //Servlet初始化的时候读取初始化参数
        //classpath:springmvc.xml
        String contextConfigLocation = this.getServletConfig().getInitParameter("contextConfigLocation");
        //创建Spring容器
        webApplicationContext = new WebApplicationContext(contextConfigLocation);
        //初始化Spring容器
        webApplicationContext.refresh();
        //初始化请求映射 /user/query -》匹配Controller -》method-》paramter
        initHandleMapping();
        System.out.println("映射：" + myHandlerList);
    }

    /**
     * 初始化请求映射
     */
    public void initHandleMapping() {
        //判断容器中有没有对象
        if (webApplicationContext.iocMap.isEmpty()) {
            throw new ContextException("容器中没有对象");
        }
        //如果有对象，判断class上边有没有Controller注解
        //获取class对象
        //从iocMap一个个遍历
        for (Map.Entry<String, Object> entry : webApplicationContext.iocMap.entrySet()) {
            //拿到value()的class对象
            Class<?> claszz = entry.getValue().getClass();
            //判断是不是controller
            if (claszz.isAnnotationPresent(Controller.class)) {
                //获取controller的所有方法
                Method[] declaredMethods = claszz.getDeclaredMethods();
                //遍历方法
                for (Method declaredMethod : declaredMethods) {
                    //判断是否有mapping映射注解
                    if (declaredMethod.isAnnotationPresent(RequestMapping.class)) {
                        //取出注解和路径(value)
                        RequestMapping requestMappingAnnotation = declaredMethod.getAnnotation(RequestMapping.class);
                        ///user/query
                        String url = requestMappingAnnotation.value();
                        myHandlerList.add(new MyHandler(url, entry.getValue(), declaredMethod));

                    }
                }
            }
        }

    }

    /**
     * 请求分发
     *
     * @param request  请求
     * @param response 响应
     */
    public void excuteDispacth(HttpServletRequest request, HttpServletResponse response) {
        MyHandler handler = getHandler(request);
        try {
            if (handler == null) {
                response.getWriter().write("<h1>NOT Found 404!!!</h1>");

            } else {
                //拿到请求路径所在的方法的参数数组
                Class<?>[] parameterTypes = handler.getMethod().getParameterTypes();
                //创建参数数组
                Object[] params = new Object[parameterTypes.length];
                //用户参数map
                Map<String, String[]> parameterMap = request.getParameterMap();
                //遍历方法的参数数组
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> parameterType = parameterTypes[i];
                    //判断然后把用户的参数赋值给handler的方法的参数
                    for (Map.Entry entry : parameterMap.entrySet()) {
                        if (("HttpServletRequest").equals(parameterType.getSimpleName())) {
                            params[i] = request;
                        } else if (("HttpServletResponse").equals(parameterType.getSimpleName())) {
                            params[i] = response;
                        }
                    }
                }

                for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                    String value = entry.getValue()[0];
                    //需要优化
                    // TODO: 2022/8/22

                    //调用方法对应下标注入
                    int index = hasRequestParam(handler.getMethod(), entry.getKey());
                    if (index != -1) {
                        params[index] = value;
                    } else {
                        //没有requestParam注解
                        List parameterNames = getParameterNames(handler.getMethod());
                        for (int i = 0; i < parameterNames.size(); i++) {
                            if (parameterNames.get(i).equals(entry.getKey())) {
                                params[i] = value;
                                break;
                            }
                        }
                    }
                }
                for (Object param : params) {
                    System.out.println(param);
                }
                Object invoke = handler.getMethod().invoke(handler.getController(), params);

                if (invoke instanceof String) {
                    String viewStr = (String) invoke;
                    System.out.println("返回参数"+viewStr);
                    //返回的字符串有没有：
                    if (viewStr.contains(":")) {
                        //拿到冒号前后的字符串
                        String[] split = ((String) invoke).split(":");
                        String viewType = split[0];
                        String viewName = split[1];
                        //如果是转发
                        if (viewType.equals("forward")) {
                            request.getRequestDispatcher("/"+viewName).forward(request, response);
                            System.out.println("已经转发"+viewName+"页面");
                            //如果是重定向
                        } else if (viewType.equals("redirect")) {
                            response.sendRedirect("/"+viewName);
                            System.out.println("已经重定向"+viewName+"页面");
                            //如果不是这两个，抛出异常
                        } else {
                            throw new ContextException("请求方式错误！请检查是否包含forward或者redirect");
                        }
                        //如果压根没有冒号。那么直接默认转发页面
                    } else {
                        request.getRequestDispatcher("/"+viewStr).forward(request, response);
                        System.out.println("已经转发"+viewStr+"页面");
                    }
                }else{
                    //不是字符串，返回json数据
                    //看方法有没有ResponsBody注解
                    boolean hasResponBody = handler.getMethod().isAnnotationPresent(ResponseBody.class);
                    if(hasResponBody){
                        //返回json
                        ObjectMapper objectMapper = new ObjectMapper();
                        //通过json工具拿到invoke返回值的对象并转为json
                        String resultJson = objectMapper.writeValueAsString(invoke);
                        //响应字符集设置避免json乱码
                        response.setContentType("utf-8");
                        PrintWriter out = response.getWriter();
                        out.print(resultJson);
                        out.flush();
                        out.close();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }

    }

    /**
     * 获取请求对应的handler
     *
     * @param req
     * @return
     */
    public MyHandler getHandler(HttpServletRequest req) {
        // /user/query
        //获取用户的请求路径
        String requestURI = req.getRequestURI();
        //遍历handler拿到url
        for (MyHandler myHandler : myHandlerList) {
            if (myHandler.getUrl().equals(requestURI)) {
                return myHandler;
            }
        }
        return null;
    }

    /**
     * 判断方法参数是否有param注解，对比value返回下标。没有找到返回-1
     *
     * @param method
     * @param requestArgs
     * @return
     */
    public int hasRequestParam(Method method, String requestArgs) {
        //获取方法的参数数组
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            if (param.isAnnotationPresent(RequestParm.class)) {
                //获得参数注解的value值
                RequestParm requestParm = param.getAnnotation(RequestParm.class);
                String RequestParam = requestParm.value();
                //用注解的value值和请求发的参数对比
                if (RequestParam.equals(requestArgs)) {
                    //如果一致就返回下标
                    return i;
                }
            }
        }
        return -1;
    }

    public List getParameterNames(Method method) {
        Parameter[] parameters = method.getParameters();
        List list = new ArrayList();
        for (Parameter parameter : parameters) {
            list.add(parameter.getName());
        }
        return list;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //请求分发处理
        excuteDispacth(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
