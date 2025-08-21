// Event listeners pour les formulaires
$(document).ready(function() {
    // Event listener pour le changement de photo lors de l'édition
    $('#editPhotoFile').on('change', function(event) {
        if (window.staffManager) {
            window.staffManager.handleEditPhotoChange(event);
        }
    });

    // Event listener pour le bouton de mise à jour
    $('#updateStaffBtn').on('click', function() {
        if (window.staffManager) {
            window.staffManager.updateStaff();
        }
    });

    // Event listener pour le bouton de suppression
    $('#confirmDeleteBtn').on('click', function() {
        if (window.staffManager) {
            window.staffManager.deleteStaff();
        }
    });

    // Event listener pour fermer les modales et réinitialiser
    $('#editStaffModal').on('hidden.bs.modal', function() {
        if (window.staffManager) {
            window.staffManager.resetEditForm();
        }
    });

    $('#deleteStaffModal').on('hidden.bs.modal', function() {
        if (window.staffManager) {
            window.staffManager.currentStaffId = null;
        }
        $('#confirmDelete').prop('checked', false);
        $('#confirmDeleteBtn').prop('disabled', true);
    });
});