package laz.dimboba.sounddetection.app

import android.app.Application
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent

@HiltAndroidApp
class SoundDetectionApp : Application()

@Module
@InstallIn(SingletonComponent::class) // Defines the component/scope where these dependencies live
object AppModule {

}