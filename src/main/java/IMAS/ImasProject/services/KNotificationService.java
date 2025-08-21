package IMAS.ImasProject.services;

import IMAS.ImasProject.model.KNotification;
import IMAS.ImasProject.repository.KNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KNotificationService {
    @Autowired
    private KNotificationRepository notificationRepository;

    public KNotification save(KNotification notification) {
        return notificationRepository.save(notification);
    }
}