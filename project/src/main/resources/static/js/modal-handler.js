
    $(document).ready(function () {
    $('[id^="cancelModal-"]').on('show.bs.modal', function (event) {
        const button = $(event.relatedTarget);
        const orderId = button.data('order-id');
        const status = button.data('status');

        const modal = $(this);
        const message = modal.find('.modal-body p');
        const reactivateForm = modal.find('form.reactivate-form');
        const deleteForm = modal.find('form.delete-form');

        // Update form actions dynamically
        reactivateForm.attr('action', `/orders/${orderId}/reactivate`);
        deleteForm.attr('action', `/orders/${orderId}/delete`);

        if (status === 'CANCELED') {
            message.text('Do you want to reactivate or delete this order?');
            reactivateForm.show();
            deleteForm.show();
        } else if (status === 'NEW') {
            message.text('Are you sure you want to cancel this order?');
            reactivateForm.hide();
            deleteForm.attr('action', `/orders/${orderId}/cancel`).show();
        } else {
            message.text('This action is not allowed.');
            reactivateForm.hide();
            deleteForm.hide();
        }
    });
});

