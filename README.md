# AndroidArchitecture
[![License](https://img.shields.io/github/license/blipinsk/RecyclerViewHeader.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)
[![](https://jitpack.io/v/vinisauter/AndroidArchitecture.svg)](https://jitpack.io/#vinisauter/AndroidArchitecture)
---
Android Processor to make easier implementation of [Android Architecture Components](https://developer.android.com/topic/libraries/architecture/)

[Android Jetpack Components](https://developer.android.com/jetpack/)


Library usage
=============

* View using ViewModel and [LiveData](https://developer.android.com/topic/libraries/architecture/livedata). Annotate Activities and Fragments with **@ViewARC** to automatically create and instantiate the ViewModels and Observables for the View. 
	* Use **@ViewModel** to bind and determinate the ViewModels that you are going to use in your View.
		>TODO: @ViewModel(factory = SomeFactoryProvider.class)
	* Use **@ObserveData** on a method to observe to a ViewModel LiveData. If you need to specify the ViewModel or LiveData: **@ObserveData(viewModel = "viewVM", liveData = "currentUser")**
* If you are **NOT** using androidannotations please call _ViewClassName_**ARC.init(this)** to bind the attributes

MainActivity.java/

```
    @ViewARC
    public class MainActivity extends AppCompatActivity {
        @ViewModel
        MainViewModelARC viewVM;
        @ViewModel
        ConfigViewModelARC configVM;

        // If you are using androidannotations you do not need this block:
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            MainActivityARC.init(this);
            afterViews();
        }

        //@AfterViews
        void afterViews() {
            viewVM.loadCurrentUser();
            viewVM.setCurrentUser(user, this, new Observer<TaskStatus>() {
                @Override
                public void onChanged(TaskStatus taskStatus) {
                    if (taskStatus.isFinished()) {
                        viewLoader.setVisibility(View.GONE);
                        if (taskStatus.getState() == TaskStatus.State.FAILED) {
                            showError(taskStatus.getError());
                        }
                    } else {
                        viewLoader.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        @ObserveData 
        // If you need to specify the ViewModel or LiveData to use:
        // @ObserveData(viewModel = "viewVM", liveData = "currentUser")
        void onUserChanged(User currentUser) {
            if (currentUser != null)
                tvName.setText(currentUser.getName());
                ...
            }
        }
        ...
    }
```

* [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel). Annotating ViewModels with **@ViewModelARC** will automatically create a proxy class _ViewModelClassName_**ARC** that you can easily use in your Views.
	* Use **@Repository** to bind Repository/DataModel classes that you need to use
```
    @ViewModelARC
    public class MainViewModel extends ViewModel {
    
        @Repository
        UserRepositoryARC repository;
    
        public final MutableLiveData<User> currentUser = new MutableLiveData<>();
        
        public LiveData<User> loadCurrentUser() {
            LiveData<TaskStatus<User>> taskStatusLiveData = repository.loadCurrentUserAsync();
            return Transformations.map(taskStatusLiveData, new Function<TaskStatus<User>, User>() {
                @Override
                public User apply(TaskStatus<User> input) {
                    return input.getResult();
                }
            });
        }
        
        public MutableLiveData<TaskStatus> setCurrentUserName(String currentUserName) {
            MutableLiveData<TaskStatus> statusUserTask = repository.sendUserNameToServer(currentUserName, new Callback<User>() {
                @Override
                public void onFinished(User user, Throwable error) {
                    if (error == null)
                        currentUser.setValue(user);
                }
            });
            return statusUserTask;
        }
    }
```
* Repository/DataModel. Here is where is the development of the business logic or back-end logic, so the hard work needs to be done in background. Annotating **@RepositoryARC** to your class, will automatically create a proxy class _RepositoryClassName_**ARC** that generates a asynchronous methods with callbacks or LiveData, by simply adding **@Async** to your method. 
	* Use **@Async** to generate asynchronous methods with callbacks or LiveData. 
		* default = @Async(value = AsyncType.ASYNC_TASK, executor = ExecutorType.SERIAL, allowMultipleCalls = false)
		* value = [AsyncType.ASYNC_TASK, AsyncType.LIVE_DATA]
		* executor = [ExecutorType.SERIAL, ExecutorType.THREAD_POOL]
		* allowMultipleCalls = [true, false]

```
    @RepositoryARC
    public class UserRepository {
    
        @Async(AsyncType.LIVE_DATA)
        public User loadCurrentUser() {
            final User user = new User("John Doe");
            return user;
        }
    
        @Async
        public User sendUserNameToServer(final String userName) throws Throwable {
            final User user = new User();
            user.setUserName(userName);
            SystemClock.sleep(5000);
            return user;
        }
    }
```
Including In Your Project
-------------------------

Add it in your root build.gradle at the end of repositories:

```groovy
allprojects {
	repositories {
        google()
        //...
        maven { url "https://jitpack.io" }
    }
}
```

Add the dependency

AndroidAnnotationsVersion: [![](https://jitpack.io/v/androidannotations/androidannotations.svg)](https://jitpack.io/#androidannotations/androidannotations)
```groovy
dependencies {

    implementation 'com.github.vinisauter.AndroidArchitecture:architecture-annotations:master'
    annotationProcessor 'com.github.vinisauter.AndroidArchitecture:architecture-processor:master'
    // If you are using androidannotations.
    implementation "org.androidannotations:androidannotations-api:4.5.2"
    annotationProcessor "org.androidannotations:androidannotations:4.5.2"
    annotationProcessor 'com.github.vinisauter.AndroidArchitecture:arcandroidannotationsplugin:master'
    //...
    // AndroidX Architecture Components
    implementation 'androidx.lifecycle:lifecycle-viewmodel:2.0.0'
    implementation 'androidx.lifecycle:lifecycle-extensions:2.0.0'
    //...
    }
```

Developed by
============
 * Vinicius Sauter
 
[![View Vinicius profile on LinkedIn](https://www.linkedin.com/img/webpromo/btn_viewmy_160x33.png)](https://www.linkedin.com/in/vinicius-de-andrade-sauter-731a2862/)

License
=======

    Copyright 2018 Vinicius Sauter
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
