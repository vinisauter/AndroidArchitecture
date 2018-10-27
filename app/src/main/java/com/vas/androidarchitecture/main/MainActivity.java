package com.vas.androidarchitecture.main;

import android.widget.TextView;

import com.vas.androidarchitecture.R;
import com.vas.androidarchitecture.generic.ConfigViewModelARC;
import com.vas.androidarchitecture.model.User;
import com.vas.architectureandroidannotations.ArcView;
import com.vas.architectureandroidannotations.ToString;
import com.vas.architectureandroidannotations.api.TaskStatus;
import com.vas.architectureandroidannotations.view.ObserveData;
import com.vas.architectureandroidannotations.view.ViewModel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.text.MessageFormat;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by Vinicius Sauter liveData 09/10/2018.
 * .
 */
@ToString
@ArcView
@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    @ViewModel("1")
    MainViewModelARC viewVM;
    @ViewModel("2")
    ConfigViewModelARC configVM;

    @ViewById
    TextView tvStatus;
    @ViewById
    TextView tvName;

    @AfterViews
    void afterViews() {
        MainActivityARC.init(this);
    }

    @ObserveData(liveData = "statusUserTask")
    void onStatusChanged(TaskStatus status) {
        if (status != null) {
            String text = MessageFormat.format("{0} {1} {2}", status.getTaskName(), status.getState().name(), status.hashCode());
            if (status.getError() != null) {
                text += (" " + status.getError().getMessage());
            }
            tvStatus.setText(MessageFormat.format("{0}\n{1}",
                    text,
                    tvStatus.getText()));
        }
    }

    @ObserveData
    void onUserChanged(User currentUser) {
        if (currentUser != null)
            tvName.setText(currentUser.getName());
    }

    @Click(R.id.bt_submit1)
    void setCurrentUserName() {
        Random gerador = new Random();
        String anotherName = "John Doe " + gerador.nextInt();
        viewVM.setCurrentUserName(anotherName);
    }

    @Click(R.id.bt_submit2)
    void saveUser() {
        Random gerador = new Random();
        String anotherName = "John Doe " + gerador.nextInt();
        User user = new User();
        user.setUserName(anotherName);
        viewVM.setCurrentUser(user);
    }

    @ObserveData(liveData = "statusTask")
    void onStatusTaskChanged(TaskStatus status) {
        if (status != null) {
            String text = MessageFormat.format("{0} {1} {2}", status.getTaskName(), status.getState().name(), status.hashCode());
            if (status.getError() != null) {
                text += (" " + status.getError().getMessage());
            }
            tvStatus.setText(MessageFormat.format("{0}\n{1}",
                    text,
                    tvStatus.getText()));
        }
    }

    @Click(R.id.bt_serial)
    void serialClick() {
        configVM.serialAsync();
    }

    @Click(R.id.bt_serial_multiple)
    void serialMultipleClick() {
        configVM.serialMultipleAsync();
    }

    @Click(R.id.bt_thread_pool)
    void threadPoolClick() {
        configVM.threadPoolAsync();
    }

    @Click(R.id.bt_thread_pool_multiple)
    void threadPoolMultipleClick() {
        configVM.threadPoolMultipleAsync();
    }
}
