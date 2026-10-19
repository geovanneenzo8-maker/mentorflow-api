const state = {
    mentors: [],
    mentees: [],
    bookings: [],
    metrics: null
};

const brlFormatter = new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL'
});

function formatCurrency(val) {
    return brlFormatter.format(Number(val || 0));
}

function formatDateTime(isoStr) {
    if (!isoStr) return '-';
    const d = new Date(isoStr);
    return d.toLocaleString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function logToInspector(method, endpoint, status, payload) {
    const badge = document.getElementById('consoleStatusBadge');
    const ep = document.getElementById('consoleEndpoint');
    const pre = document.getElementById('consolePayload');

    badge.textContent = `HTTP ${status}`;
    badge.className = 'http-badge ' + (status >= 200 && status < 300 ? 'http-200' : status === 409 ? 'http-409' : 'http-422');
    ep.textContent = `${method} ${endpoint}`;
    pre.textContent = JSON.stringify(payload, null, 2);
}

async function apiRequest(method, url, body = null) {
    const opts = {
        method,
        headers: { 'Content-Type': 'application/json' }
    };
    if (body) {
        opts.body = JSON.stringify(body);
    }
    const res = await fetch(url, opts);
    const data = await res.json().catch(() => ({}));
    logToInspector(method, url, res.status, data);
    return { ok: res.ok, status: res.status, data };
}

async function refreshAll() {
    const specialty = document.getElementById('specialtyFilter').value;
    const mentorQuery = specialty ? `?specialty=${encodeURIComponent(specialty)}` : '';

    const [metricsRes, mentorsRes, menteesRes, bookingsRes] = await Promise.all([
        fetch('/api/v1/dashboard/metrics').then(r => r.json()),
        fetch(`/api/v1/mentors${mentorQuery}`).then(r => r.json()),
        fetch('/api/v1/mentees').then(r => r.json()),
        fetch('/api/v1/bookings').then(r => r.json())
    ]);

    state.metrics = metricsRes;
    state.mentors = mentorsRes;
    state.mentees = menteesRes;
    state.bookings = bookingsRes;

    renderMetrics();
    renderMentees();
    renderMentors();
    renderBookings();
    renderLedger();
    populateModalSelects();
}

function renderMetrics() {
    const m = state.metrics;
    if (!m) return;
    document.getElementById('metricGmv').textContent = formatCurrency(m.grossMerchandiseVolume);
    document.getElementById('metricMentorPayout').textContent = formatCurrency(m.mentorTotalPayouts);
    document.getElementById('metricPlatformRev').textContent = formatCurrency(m.platformNetRevenue);
    document.getElementById('metricBookingsCount').textContent = m.totalBookings;
    document.getElementById('metricBookingsBreakdown').textContent =
        `${m.confirmedBookings} confirmadas • ${m.completedBookings} concluídas • ${m.pendingBookings} pendentes • ${m.cancelledBookings} canceladas`;
}

function renderMentees() {
    const container = document.getElementById('menteesContainer');
    container.innerHTML = state.mentees.map(mentee => `
        <div class="mentee-card">
            <div class="mentee-info">
                <h4>${mentee.name}</h4>
                <span>${mentee.roleTarget} (${mentee.careerLevel})</span>
            </div>
            <div class="mentee-wallet">
                <strong>${formatCurrency(mentee.walletBalance)}</strong>
                <button class="btn btn-outline btn-sm" onclick="topUpMentee(${mentee.id})" style="margin-top:4px;">
                    + R$ 500
                </button>
            </div>
        </div>
    `).join('');
}

function renderMentors() {
    const container = document.getElementById('mentorsContainer');
    container.innerHTML = state.mentors.map(mentor => `
        <div class="mentor-card">
            <div>
                <div class="mentor-top">
                    <div>
                        <div class="mentor-name">${mentor.name}</div>
                        <div class="mentor-role">${mentor.title} @ ${mentor.company}</div>
                    </div>
                    <div class="mentor-rate">${formatCurrency(mentor.hourlyRate)}/h</div>
                </div>
                <p class="mentor-bio" style="margin-top:10px;">${mentor.bio}</p>
                <div class="skills-list" style="margin-top:12px;">
                    ${(mentor.skills || []).map(s => `<span class="skill-tag">${s}</span>`).join('')}
                </div>
            </div>
            <div class="mentor-footer">
                <div class="mentor-stats">
                    <span>⭐ <strong>${Number(mentor.averageRating).toFixed(1)}</strong> (${mentor.totalReviews} avaliações) • ${mentor.totalSessionsCompleted} sessões</span>
                    <span>Carteira Mentor: <span class="mentor-wallet-pill">${formatCurrency(mentor.walletBalance)}</span></span>
                </div>
                <button class="btn btn-primary btn-sm" onclick="openBookingForMentor(${mentor.id})">
                    Agendar Horário
                </button>
            </div>
        </div>
    `).join('');
}

function renderBookings() {
    const tbody = document.getElementById('bookingsTableBody');
    tbody.innerHTML = state.bookings.map(b => {
        let actionsHtml = '';
        if (b.status === 'PENDING_PAYMENT') {
            actionsHtml = `
                <button class="btn btn-emerald btn-sm" onclick="confirmBookingPayment(${b.id})">
                    💳 Confirmar & Split (85/15)
                </button>
                <button class="btn btn-danger btn-sm" onclick="cancelBooking(${b.id})">
                    Cancelar
                </button>
            `;
        } else if (b.status === 'CONFIRMED') {
            actionsHtml = `
                <button class="btn btn-primary btn-sm" onclick="completeBooking(${b.id})">
                    ✅ Concluir Mentoria
                </button>
                <button class="btn btn-danger btn-sm" onclick="cancelBooking(${b.id})">
                    ↩️ Cancelar c/ Estorno
                </button>
            `;
        } else if (b.status === 'COMPLETED') {
            actionsHtml = `
                <span style="font-size:0.76rem;color:#34d399;font-weight:600;">✔ Finalizada</span>
            `;
        } else {
            actionsHtml = `
                <span style="font-size:0.75rem;color:#9ca3af;">Horário liberado</span>
            `;
        }

        return `
            <tr>
                <td class="mono">#${b.id}</td>
                <td>
                    <strong>${b.mentorName}</strong>
                    <div style="font-size:0.75rem;color:#9ca3af;">${b.mentorCompany}</div>
                </td>
                <td>${b.menteeName}</td>
                <td style="max-width:250px;">
                    <div style="font-weight:500;">${b.topic}</div>
                    ${b.meetingLink ? `<a href="${b.meetingLink}" target="_blank" style="font-size:0.73rem;color:#818cf8;">🔗 Sala de Vídeo</a>` : ''}
                </td>
                <td class="mono" style="font-size:0.78rem;">
                    ${formatDateTime(b.startTime)}<br>
                    <span style="color:#9ca3af;">(${b.durationMinutes} min)</span>
                </td>
                <td class="mono" style="font-size:0.78rem;">
                    <strong>${formatCurrency(b.totalAmount)}</strong>
                    <div style="color:#34d399;">M: ${formatCurrency(b.mentorAmount)}</div>
                    <div style="color:#818cf8;">P: ${formatCurrency(b.platformFee)}</div>
                </td>
                <td>
                    <span class="status-badge status-${b.status}">${b.status}</span>
                </td>
                <td>
                    <div class="action-group">${actionsHtml}</div>
                </td>
            </tr>
        `;
    }).join('');
}

function renderLedger() {
    const tbody = document.getElementById('ledgerTableBody');
    const txs = (state.metrics && state.metrics.recentTransactions) || [];
    tbody.innerHTML = txs.map(tx => {
        const isRefund = tx.type === 'REFUND_DEBIT';
        const color = isRefund ? '#fb7185' : tx.type === 'MENTOR_SPLIT_CREDIT' ? '#34d399' : '#a5b4fc';
        return `
            <tr>
                <td class="mono" style="font-size:0.76rem;color:#9ca3af;">${tx.referenceCode}</td>
                <td class="mono">#${tx.bookingId}</td>
                <td>
                    <span class="status-badge" style="background:rgba(255,255,255,0.06);color:${color};">
                        ${tx.type}
                    </span>
                </td>
                <td>${tx.description}</td>
                <td class="mono" style="font-weight:700;color:${color};">
                    ${isRefund ? '- ' : '+ '}${formatCurrency(tx.amount)}
                </td>
                <td class="mono" style="font-size:0.76rem;color:#9ca3af;">${formatDateTime(tx.createdAt)}</td>
            </tr>
        `;
    }).join('');
}

function populateModalSelects() {
    const mentorSelect = document.getElementById('formMentorId');
    const menteeSelect = document.getElementById('formMenteeId');

    const currentMentor = mentorSelect.value;
    mentorSelect.innerHTML = state.mentors.map(m =>
        `<option value="${m.id}">${m.name} (${m.company}) — ${formatCurrency(m.hourlyRate)}/h</option>`
    ).join('');
    if (currentMentor) mentorSelect.value = currentMentor;

    menteeSelect.innerHTML = state.mentees.map(s =>
        `<option value="${s.id}">${s.name} (Saldo: ${formatCurrency(s.walletBalance)})</option>`
    ).join('');
}

async function confirmBookingPayment(bookingId) {
    await apiRequest('POST', `/api/v1/bookings/${bookingId}/confirm-payment`);
    await refreshAll();
}

async function completeBooking(bookingId) {
    await apiRequest('POST', `/api/v1/bookings/${bookingId}/complete`);
    await refreshAll();
}

async function cancelBooking(bookingId) {
    await apiRequest('POST', `/api/v1/bookings/${bookingId}/cancel`, {
        reason: 'Cancelamento solicitado via painel de operações com estorno automático.'
    });
    await refreshAll();
}

async function topUpMentee(menteeId) {
    await apiRequest('POST', `/api/v1/mentees/${menteeId}/topup?amount=500.00`);
    await refreshAll();
}

function openBookingForMentor(mentorId) {
    document.getElementById('formMentorId').value = String(mentorId);
    document.getElementById('bookingModal').classList.remove('hidden');
}

// Simulações de Engenharia Backend em 1 clique
document.getElementById('btnSimulateConflict').addEventListener('click', async () => {
    const activeBooking = state.bookings.find(b => b.status === 'CONFIRMED' || b.status === 'PENDING_PAYMENT');
    if (!activeBooking) {
        alert('Crie um agendamento primeiro para simular conflito de horário.');
        return;
    }
    await apiRequest('POST', '/api/v1/bookings', {
        mentorId: activeBooking.mentorId,
        menteeId: activeBooking.menteeId,
        topic: 'Tentativa Concorrente no Mesmo Horário (Teste Double-Booking)',
        notes: 'Disparo de teste para demonstrar bloqueio de sobreposição temporal (409 Conflict - RFC 7807).',
        startTime: activeBooking.startTime,
        endTime: activeBooking.endTime
    });
});

document.getElementById('btnSimulateRuleError').addEventListener('click', async () => {
    const firstMentor = state.mentors[0];
    const firstMentee = state.mentees[0];
    if (!firstMentor || !firstMentee) return;

    const tomorrow = new Date(Date.now() + 86400000).toISOString().split('T')[0];
    await apiRequest('POST', '/api/v1/bookings', {
        mentorId: firstMentor.id,
        menteeId: firstMentee.id,
        topic: 'Teste de Sessão com Duração Inválida (10 minutos)',
        notes: 'Demonstração de validação de invariantes de domínio (422 Unprocessable Entity).',
        startTime: `${tomorrow}T15:00:00`,
        endTime: `${tomorrow}T15:10:00`
    });
});

document.getElementById('specialtyFilter').addEventListener('change', refreshAll);

document.getElementById('btnOpenNewBookingModal').addEventListener('click', () => {
    document.getElementById('bookingModal').classList.remove('hidden');
});

document.getElementById('btnCloseModal').addEventListener('click', () => {
    document.getElementById('bookingModal').classList.add('hidden');
});

document.getElementById('btnCancelModal').addEventListener('click', () => {
    document.getElementById('bookingModal').classList.add('hidden');
});

document.getElementById('bookingForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const date = document.getElementById('formDate').value;
    const start = document.getElementById('formStartTime').value;
    const end = document.getElementById('formEndTime').value;

    const payload = {
        mentorId: Number(document.getElementById('formMentorId').value),
        menteeId: Number(document.getElementById('formMenteeId').value),
        topic: document.getElementById('formTopic').value,
        notes: document.getElementById('formNotes').value,
        startTime: `${date}T${start}:00`,
        endTime: `${date}T${end}:00`,
        idempotencyKey: document.getElementById('formIdempotency').value || null
    };

    const res = await apiRequest('POST', '/api/v1/bookings', payload);
    if (res.ok) {
        document.getElementById('bookingModal').classList.add('hidden');
        await refreshAll();
    }
});

// Inicializa a data padrão do modal para daqui a 5 dias
const defaultDate = new Date(Date.now() + 5 * 86400000).toISOString().split('T')[0];
document.getElementById('formDate').value = defaultDate;

refreshAll();
