import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;

public class Main {
    //Text
    final static String LogName = "Сообщения";
    final static String Error = "Ошибка";
    final static String StatusName = "Статус";
    final static String SendName = "Отправить";
    final static String RefreshName = "Обновить";
    final static String ConnectName = "Подключиться";
    final static String ClientsName = "Пользователи";
    final static String DisconnectName = "Отключиться";
    final static String ClientsNo = "Нет пользователей";
    final static String BusyServerConnection = "Пользователь уже подключен";
    final static String[] NeedChooseUser = new String[]{"Не выбран пользователь для подключения",
            "Нажмите \"" + Main.RefreshName + "\" для получения списка пользователей",
            "и выберите пользователя"};

    //Regex
    final static int limitLoops = 100;
    final static Long[] SimpleNumbers = simpleGen(1000);

    //Status
    final static String rsaStatusNames = "P: \nQ: \nМодуль: \nФункция Эйлера: \nОткрытая экспонента: \nD: \nПолученная экспонента: \nПолученный модуль: ";

    //Ports
    final static int EnvironmentPort = 5999;
    final static int BeginPortInterval = 6000;
    final static int EndPortInterval = 6500;

    final static boolean LogEnv = false;

    public static void main(String[] args) {
        localizationGUI();

        new Environment(EnvironmentPort);       //6000-6500
        new Client("Alisa", 6001);  //1
        new Client("Bob", 6007);    //2
        new Client("Karl", 6005);   //3
        /*new Client("Kevin", 6010);  //4
        new Client("Piter", 6006);  //5
        new Client("Bill", 6450);   //6
        new Client("David", 6015);  //7
        new Client("Sarah", 6012);  //8*/
    }

    private static void localizationGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

        UIManager.put("OptionPane.yesButtonText", "Да");
        UIManager.put("OptionPane.noButtonText", "Нет");
        UIManager.put("OptionPane.cancelButtonText", "Отмена");
        UIManager.put("OptionPane.okButtonText", "Готово");

        FontUIResource f = new FontUIResource(new Font("Verdana", Font.PLAIN, 12));
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object val = UIManager.get(key);
            if (val instanceof FontUIResource) {
                FontUIResource orig = (FontUIResource) val;
                Font font = new Font(f.getFontName(), orig.getStyle(), f.getSize());
                UIManager.put(key, new FontUIResource(font));
            }
        }
    }

    private static Long[] simpleGen(int limitNumber) {
        Long[] simpleNumbers = new Long[limitNumber];
        ArrayList<Long> sns = new ArrayList<>();

        for (int i = 2; i * i < limitNumber; i++) {
            if (simpleNumbers[i] == null) {
                for (int j = i * i; j < limitNumber; j += i) {
                    simpleNumbers[j] = Integer.toUnsignedLong(0);
                }
            }
        }
        for (int i = 2; i < simpleNumbers.length; i++) {
            if (simpleNumbers[i] == null) {
                sns.add(Integer.toUnsignedLong(i));
            }
        }
        return sns.toArray(new Long[0]);
    }
}
