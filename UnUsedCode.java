package search;

/**
 * @author xiangyn
 * @date 2018/1/26
 * @since 1.0
 */

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class UnUsedCode {

    /**
     * key - 类名  value - 方法名
     */
    private static Map<String, List<String>> classMap = new HashMap<String, List<String>>(512);

    /**
     * classMap的复制map
     */
    private static Map<String, List<String>> copyClassMap = new HashMap<String, List<String>>(512);

    /**
     * key - 文件夹  value - 类名
     */
    private static Map<String, List<String>> fileMap = new TreeMap<String, List<String>>();


    private final static int maxLength = Character.MAX_VALUE;

    private HashSet<String>[] keySearchFile = new HashSet[maxLength];

    /**
     * 接口文件的路径  绝对路径  暂时只能实现 实现类 + 接口 的目录结构
     */
    private String getPathname(){
        String filePath = "D:\\workspace2\\Java-Rest\\rest-db\\src\\main\\java\\com\\angho\\rest\\mapper\\rest"; //"D:\\workspace2\\Java-Rest\\rest-service\\src\\main\\java\\com\\angho\\rest\\service";//
        return filePath;
    }

    /**
     * 指定调用接口的路径名 绝对路径
     * 多个调用路径之间使用英文逗号隔开
     */
    private String transferPath(){
        //例 多个文件路径中间逗号隔开
        String filePath = "D:\\workspace2\\Java-Rest\\rest-db\\src\\main\\java\\com\\angho\\rest\\dao";//"D:\\workspace2\\Java-Rest\\rest-service\\src\\main\\java\\com\\angho\\rest,D:\\workspace2\\Java-Rest\\rest-manager\\src\\main\\java\\com\\angho\\rest";//
        return filePath;
    }

    /**
     * 默认的结果输出路径
     */
    private String outputResultPath() {
        String filepath = "c:\\UnUserCode";
        return filepath;
    }

    /**
     * 读取文件中的内容
     * @throws FileNotFoundException
     */
    private void getFileMethod() {
        String filePath = this.getPathname();
        File file = new File(filePath);
        if(file.exists()){
            File[] files = file.listFiles();

            if(files.length == 0) {
                return;
            } else {
                for(File file2 : files) {
                    String itfFileName = file2.getName();
                    List<String> fileList = new ArrayList<String>();
                    //向下一层遍历文件目录,遍历所有的接口
                    if(file2.isDirectory()){
                        String[] fileNameList = file2.list();
                        for(String fileName : fileNameList){
                            File subFile = new File(file2.getPath(), fileName);

                            //实现类不进行处理,直接跳出
                            if(subFile.isDirectory()){
                                continue;
                            }
                            fileList.add(fileName.substring(0, fileName.lastIndexOf(".")));
                            this.readFileByLines(subFile);
                        }
                        fileMap.put(itfFileName, fileList);
                    }
                }
            }
        }
    }

    /**
     * 以行为单位读取文件,并存储单行的key - value
     */
    private void readFileByLines(File file){
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;

            //判断是否为接口开始的首行
            boolean isFistLine = true;
            String name = null;
            List<String> methodList = new ArrayList<String>();
            List<String> methodListcopy = new ArrayList<String>();
            //正则表达式规则  待改进
            String regex = "^.*(\\(.*\\).*;$)";
            //String regex = "^.*(\\);$)";
            while((tempString = reader.readLine()) != null) {

                if(tempString.matches("^.*(\\\\).*")){
                    continue;
                }
                //需改为正则表达式处理字符串  ----------------------
                //正则表达式
                if(!isFistLine){
                    if (tempString.matches(regex)) {
                        String[] str = tempString.split("\\(");
                        String[] singles = str[0].split("\\s+");
                        String method = singles[singles.length - 1];
                        methodList.add(method);
                        methodListcopy.add(method);
                    }
                }

                if(isFistLine){
                    //
                    //int isS = tempString.indexOf(regex);

                    if(tempString.matches("^.*interface.*")){
                        String[] singles = tempString.split("\\s+");
                        if (singles.length > 2) {
                            name = singles[2];
                            isFistLine = false;
                        }
                        //methodMap.put(name, Collections.EMPTY_SET);
                    }
                }
            }
            if (name != null) {
                classMap.put(name, methodList);
                copyClassMap.put(name, methodListcopy);
            }

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 找出接口中哪些方法是没有被调用
     * 暂时不用
     */
    private void verifiMethod(){
        //优化 优化 优化！ 重要的事情说三遍
        //多个文件路劲以逗号分割
        String transferPath = this.transferPath();
        String[] transferPaths = transferPath.split("\\,");
        for(int i = 0; i < transferPaths.length; i++) {
            File file = new File(transferPaths[i]);
            if(file.exists()){
                File[] files = file.listFiles();

                if(files.length == 0) {
                    return;
                }

                File subFile = null;
                File implFile = null;
                for(File file2 : files) {
                    //只遍历
                    if (file2.isDirectory()) {
                        String[] file2List = file2.list();
                        for (String fileNme : file2List) {
                            subFile = new File(file2.getPath(), fileNme);
                            // this.verifiFileProcess(subFile);
                            //循环遍历
                            if(subFile.isDirectory()){
                                String[] subFileList = subFile.list();
                                for (String subFileName : subFileList) {
                                    implFile = new File(subFile.getPath(), subFileName);
                                    this.verifiFileProcess(implFile);
                                }
                            }
                        }
                    }
                }

            }
        }

    }

    /**
     * 排除接口中哪些方法是没有被调用
     * 有些目录结构不是是统一的按 实现类 + 接口, 对于这些目录直接进行全部遍历,
     */
    private void allVerifiMethod(){
        //多个文件路径以逗号分割
        String transferPath = this.transferPath();
        String[] transferPaths = transferPath.split("\\,");
        for(int i = 0; i < transferPaths.length; i++) {
            File file = new File(transferPaths[i]);
            if (file.exists()) {
                this.allVerifiMethodDT(file);
            }
        }
        //this.verifiFileProcess(implFile);
        this.outputResult();
    }


    private void allVerifiMethodDT(File file){
        File[] files = file.listFiles();

        if(files.length == 0) {
            return;
        }

        for(File subFile : files) {

            if (subFile.isDirectory()) {
                this.allVerifiMethodDT(subFile);
            }

            if (subFile.isFile()) {
                this.verifiFileProcess(subFile);
            }
        }
    }


    /**
     * 单个实现类中调用情况
     */
    private void verifiFileProcess(File file){
        BufferedReader reader = null;
        //临时存储类和方法名   key - 类名
        Map<String, List<String>> snapMap = new HashMap<>();
        //类名 和 其对象名   key - 对象名  value 类名
        Map<String, String> associatedMap = new HashMap<>();
        try{
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;

            //判断接下去一行是否有类名需要保存
            boolean isSaveItf = false;
            //判断是否为方法的开始
            boolean isMethodStart = false;

            //正则表达式规则  待改进  以import的方式
            String regex = ".*@Resource.*";
            while((tempString = reader.readLine()) != null) {
                //碰到是接口的文件直接跳过
                if(tempString.matches(".*interface.*")){
                    break;
                }

                if(!specialLine(tempString)){
                    continue;
                }

                if (!isMethodStart) {

                    if (tempString.matches(regex)) {
                        isSaveItf = true;
                        continue;
                    }

                    if (isSaveItf) {
                        String temp = tempString.replaceAll("\\pP", "");
                        String singles[] = temp.split("\\s+");
                        if (singles.length > 3) {
                            String cName = singles[2];
                            if (classMap.get(cName) != null) {
                                snapMap.put(singles[3], classMap.get(cName));
                                associatedMap.put(singles[3], cName);
                            }
                        }
                        isSaveItf = false;
                        continue;
                    }
                }

                //
                if (!isMethodStart && tempString.matches("^.*(\\).*\\{.*)$"))
                {
                    isMethodStart = true;
                    continue;
                }
                if (isMethodStart) {
                    Iterator<Map.Entry<String, List<String>>> entries = snapMap.entrySet().iterator();
                    while (entries.hasNext()) {
                        Map.Entry<String, List<String>> entry = entries.next();
                        if(tempString.matches(".*" + entry.getKey() + ".*")){
                            String rgex = entry.getKey() + "\\.(.*?)\\(";
                            Pattern pattern = Pattern.compile(rgex);// 匹配的模式
                            Matcher m = pattern.matcher(tempString);

                            boolean is = tempString.matches(rgex);
                            String useMethod = null;

                            while (m.find()) {
                                useMethod = m.group(1);
                            }

                            if (useMethod != null) {
                                List<String> str = snapMap.get(entry.getKey());
                                str.remove(useMethod);
                                if (str.size() == 0) {
                                    classMap.remove(associatedMap.get(entry.getKey()));
                                }
                            }
                        }
                    }
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 将最终结果输出到txt文本中
     */
    private void outputResult(){
        //先将map排序在输出
        //this.sortFile();
        String filePath = this.outputResultPath();
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            FileWriter fw = new FileWriter(filePath + "\\result.txt");
            BufferedWriter bfWriter = new BufferedWriter(fw);
            Iterator<Map.Entry<String, List<String>>> entries = fileMap.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, List<String>> entry = entries.next();
                List<String> list = entry.getValue();
                if (list.size() != 0) {
                    bfWriter.write("文件夹  " + entry.getKey());
                    bfWriter.write("\r\n");
                    for (String str : list) {
                        if (classMap.get(str) != null) {
                            bfWriter.write(str + "  未调用的方法: ");
                            for (String method : classMap.get(str)) {
                                bfWriter.write(method + "  ");
                            }
                            if(classMap.get(str).size() == copyClassMap.get(str).size()){
                                bfWriter.write("   //此类可以弃用");
                            }
                            bfWriter.write("\r\n");
                            bfWriter.write("\r\n");
                        }
                    }
                    bfWriter.write("----------------------------------------------------------------------------灵魂分割线");
                    bfWriter.write("\r\n");
                }
            }
            bfWriter.close();
            fw.close();
        } catch (IOException e) {
            e.getMessage();
        }

    }

    /**
     * xml解析
     */
    private void xmlAnalyze(){
        //
    }


    /**
     * 将fileMap进行排序
     */
    private void sortFileMap() {
        List<Map.Entry<String,List<String>>> list = new ArrayList<Map.Entry<String,List<String>>>(fileMap.entrySet());
        // 通过比较器实现排序
        Collections.sort(list, new Comparator<Map.Entry<String, List<String>>>() {
            @Override
            public int compare(Map.Entry<String, List<String>> o1, Map.Entry<String, List<String>> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
    }


    /**
     * 处理一些特殊情况
     */
    private boolean specialLine(String tempString){
        boolean isSuccess = true;

        //空白行直接跳过
        if(tempString.length() == 0){
            isSuccess = false;
            return isSuccess;
        }

        //碰到//直接跳过
        if(tempString.matches("^.*(\\\\).*")){
            isSuccess = false;
            return isSuccess;
        }

        return isSuccess;
    }














    public static void main(String args[]) throws IOException{
        UnUsedCode unUsed = new UnUsedCode();
        unUsed.getFileMethod();

        //unUsed.verifiMethod();
        unUsed.allVerifiMethod();

        System.out.println(classMap);
    }

}
