package org.jy.jamye.domain.user.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.FileInputStream


@Service
class FcmService {
    @Value("\${fire_base.file}")
    private val serviceAccountFilePath: String? = null

    @Value("\${fire_base.fire_base_id}")
    private val projectId: String? = null


    // 의존성 주입이 이루어진 후 초기화를 수행한다.
    @PostConstruct
    fun initialize() {
        //Firebase 프로젝트 정보를 FireBaseOptions에 입력해준다.
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(FileInputStream(serviceAccountFilePath!!)))
            .setProjectId(projectId)
            .build()


        //입력한 정보를 이용하여 initialze 해준다.
        FirebaseApp.initializeApp(options)
    }

    // 받은 token을 이용하여 fcm를 보내는 메서드
    @Throws(FirebaseMessagingException::class)
    fun sendMessageByToken(title: String, body: String?, token: String) {
        FirebaseMessaging.getInstance().send(
            Message.builder()
                .setNotification(
                    Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build()
                )
                .setToken(token)
                .build()
        )
    }
}