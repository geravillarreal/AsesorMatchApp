(function(){
    document.addEventListener('DOMContentLoaded', function(){
        if (!window.EventSource) return;
        const container = document.getElementById('notificationsContainer');
        const evtSource = new EventSource('/notification/stream');
        evtSource.addEventListener('notification', function(e){
            const data = JSON.parse(e.data);
            addNotification(data.id, data.message);
        });
        function addNotification(id, message){
            let cont = document.getElementById('notificationsContainer');
            if(!cont){
                cont = document.createElement('div');
                cont.id = 'notificationsContainer';
                cont.className = 'toast-container position-fixed top-0 end-0 p-3';
                document.body.appendChild(cont);
            }
            const wrapper = document.createElement('div');
            wrapper.className = 'toast align-items-center text-bg-info';
            wrapper.setAttribute('data-bs-autohide','false');
            wrapper.innerHTML =
                '<div class="d-flex">' +
                '<div class="toast-body"><span>'+message+'</span></div>' +
                '<form action="/notification/delete" method="post" style="display:inline;">' +
                '<input type="hidden" name="id" value="'+id+'" />' +
                '<button type="submit" class="btn-close btn-close-white me-2 m-auto" aria-label="Close"></button>' +
                '</form>' +
                '</div>';
            cont.appendChild(wrapper);
            new bootstrap.Toast(wrapper).show();
        }
    });
})();

