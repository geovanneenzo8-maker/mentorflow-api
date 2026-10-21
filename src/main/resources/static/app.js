const state = {
    mentors: [],
    mentees: [],
    bookings: [],
    metrics: null,
    demoMode: false
};

// Estado inicial para modo demonstração estática (GitHub Pages) caso o backend Spring Boot não esteja local
const demoStore = {
    mentors: [
        {
            id: 1,
            name: "Rafael Mendes",
            email: "rafael.mendes@mentorflow.dev",
            title: "Staff Backend Engineer",
            company: "Itaú Unibanco",
            bio: "Especialista em arquitetura Java/Spring Boot para sistemas bancários de missão crítica, concorrência, mensageria com Kafka e modelagem relacional.",
            specialty: "BACKEND_JAVA",
            skills: ["Java 21", "Spring Boot 3", "JPA/Hibernate", "Kafka", "PostgreSQL", "Testes Integrados"],
            hourlyRate: 180.00,
            walletBalance: 306.00,
            averageRating: 5.0,
            totalReviews: 1,
            totalSessionsCompleted: 1
        },
        {
            id: 2,
            name: "Camila Rocha",
            email: "camila.rocha@mentorflow.dev",
            title: "Principal Software Architect",
            company: "Nubank",
            bio: "Focada em sistemas distribuídos de alta escala, idempotência financeira, motores de ledger imutável e preparação para entrevistas de System Design.",
            specialty: "SYSTEM_DESIGN",
            skills: ["System Design", "Microsserviços", "Consistência Eventual", "Resiliência", "Arquitetura Limpa"],
            hourlyRate: 240.00,
            walletBalance: 306.00,
            averageRating: 5.0,
            totalReviews: 1,
            totalSessionsCompleted: 1
        },
        {
            id: 3,
            name: "Lucas Ferreira",
            email: "lucas.ferreira@mentorflow.dev",
            title: "Senior Cloud & SRE Engineer",
            company: "Mercado Livre",
            bio: "Engenheiro de plataforma e confiabilidade. Ajudo desenvolvedores backend a dominarem Docker, Kubernetes, CI/CD e observabilidade em produção.",
            specialty: "CLOUD_DEVOPS",
            skills: ["Docker", "Kubernetes", "AWS", "GitHub Actions", "Prometheus", "OpenTelemetry"],
            hourlyRate: 190.00,
            walletBalance: 0.00,
            averageRating: 5.0,
            totalReviews: 0,
            totalSessionsCompleted: 0
        },
        {
            id: 4,
            name: "Juliana Martins",
            email: "juliana.martins@mentorflow.dev",
            title: "Lead Data & AI Engineer",
            company: "iFood",
            bio: "Lidero pipelines de dados em tempo real e integração de modelos de IA em produtos de alto tráfego. Mentoria prática em SQL avançado e arquitetura de dados.",
            specialty: "DATA_ENGINEERING",
            skills: ["SQL Tuning", "Apache Spark", "Python", "PostgreSQL", "Engenharia de Dados", "IA Generativa"],
            hourlyRate: 210.00,
            walletBalance: 0.00,
            averageRating: 5.0,
            totalReviews: 0,
            totalSessionsCompleted: 0
        },
        {
            id: 5,
            name: "Bruno Oliveira",
            email: "bruno.oliveira@mentorflow.dev",
            title: "Engineering Manager",
            company: "Santander Tecnologia",
            bio: "Mais de 120 entrevistas técnicas conduzidas para vagas de Estágio, Júnior e Pleno. Revisão de código, estruturação de portfólio e simulação de entrevista.",
            specialty: "CAREER_INTERVIEW",
            skills: ["Entrevistas Técnicas", "Clean Code", "SOLID", "Portfólio GitHub", "Carreira Backend"],
            hourlyRate: 150.00,
            walletBalance: 0.00,
            averageRating: 5.0,
            totalReviews: 0,
            totalSessionsCompleted: 0
        }
    ],
    mentees: [
        { id: 1, name: "Gabriel Almeida", email: "gabriel.almeida@dev.br", roleTarget: "Desenvolvedor Backend Java Júnior", careerLevel: "Júnior / Estágio", walletBalance: 1620.00 },
        { id: 2, name: "Mariana Costa", email: "mariana.costa@dev.br", roleTarget: "Engenheira de Software Júnior", careerLevel: "Estágio", walletBalance: 1040.00 },
        { id: 3, name: "Pedro Henrique", email: "pedro.henrique@dev.br", roleTarget: "Desenvolvedor Full-Stack / Backend", careerLevel: "Transição de Carreira", walletBalance: 1420.00 }
    ],
    bookings: [
        {
            id: 4, mentorId: 3, mentorName: "Lucas Ferreira", mentorCompany: "Mercado Livre",
            menteeId: 1, menteeName: "Gabriel Almeida",
            topic: "Containerização de APIs Spring Boot com Docker Compose e PostgreSQL",
            notes: "Configurar ambiente pronto para produção e healthcheck com Spring Actuator.",
            startTime: "2026-10-23T20:00:00", endTime: "2026-10-23T21:00:00", durationMinutes: 60,
            status: "PENDING_PAYMENT", totalAmount: 190.00, mentorAmount: 161.50, platformFee: 28.50,
            meetingLink: null, idempotencyKey: "seed-booking-004"
        },
        {
            id: 3, mentorId: 1, mentorName: "Rafael Mendes", mentorCompany: "Itaú Unibanco",
            menteeId: 3, menteeName: "Pedro Henrique",
            topic: "Simulação de Entrevista Técnica Java + Revisão de Testes JUnit/MockMvc",
            notes: "Praticar perguntas de transações (@Transactional), isolamento no banco e APIs REST.",
            startTime: "2026-10-20T19:00:00", endTime: "2026-10-20T20:00:00", durationMinutes: 60,
            status: "CONFIRMED", totalAmount: 180.00, mentorAmount: 153.00, platformFee: 27.00,
            meetingLink: "https://meet.mentorflow.dev/sala-3-a8f91c2b", idempotencyKey: "seed-booking-003"
        },
        {
            id: 2, mentorId: 2, mentorName: "Camila Rocha", mentorCompany: "Nubank",
            menteeId: 2, menteeName: "Mariana Costa",
            topic: "System Design: Modelando Carteira Digital, Split de Pagamento e Ledger Imutável",
            notes: "Entender como fintechs garantem consistência transacional sem perder centavos.",
            startTime: "2026-10-17T18:00:00", endTime: "2026-10-17T19:30:00", durationMinutes: 90,
            status: "COMPLETED", totalAmount: 360.00, mentorAmount: 306.00, platformFee: 54.00,
            meetingLink: "https://meet.mentorflow.dev/sala-2-7d3e901a", idempotencyKey: "seed-booking-002"
        },
        {
            id: 1, mentorId: 1, mentorName: "Rafael Mendes", mentorCompany: "Itaú Unibanco",
            menteeId: 1, menteeName: "Gabriel Almeida",
            topic: "Arquitetura Spring Boot, Prevenção de N+1 no JPA e Locks de Concorrência",
            notes: "Revisar projeto de portfólio antes de enviar para vagas de Backend Júnior.",
            startTime: "2026-10-16T19:00:00", endTime: "2026-10-16T20:00:00", durationMinutes: 60,
            status: "COMPLETED", totalAmount: 180.00, mentorAmount: 153.00, platformFee: 27.00,
            meetingLink: "https://meet.mentorflow.dev/sala-1-4c2b819e", idempotencyKey: "seed-booking-001"
        }
    ],
    transactions: [
        { id: 9, bookingId: 3, type: "PLATFORM_FEE_CREDIT", amount: 27.00, description: "Take-rate 15% retido pela plataforma MentorFlow", referenceCode: "TX-3-90142-FEE", createdAt: "2026-10-18T22:10:00" },
        { id: 8, bookingId: 3, type: "MENTOR_SPLIT_CREDIT", amount: 153.00, description: "Split 85% creditado na carteira do mentor Rafael Mendes", referenceCode: "TX-3-90142-MNT", createdAt: "2026-10-18T22:10:00" },
        { id: 7, bookingId: 3, type: "PAYMENT_CAPTURED", amount: 180.00, description: "Pagamento capturado de Pedro Henrique para mentoria #3", referenceCode: "TX-3-90142-CAP", createdAt: "2026-10-18T22:10:00" },
        { id: 6, bookingId: 2, type: "PLATFORM_FEE_CREDIT", amount: 54.00, description: "Take-rate 15% retido pela plataforma MentorFlow", referenceCode: "TX-2-84120-FEE", createdAt: "2026-10-17T17:50:00" },
        { id: 5, bookingId: 2, type: "MENTOR_SPLIT_CREDIT", amount: 306.00, description: "Split 85% creditado na carteira do mentor Camila Rocha", referenceCode: "TX-2-84120-MNT", createdAt: "2026-10-17T17:50:00" },
        { id: 4, bookingId: 2, type: "PAYMENT_CAPTURED", amount: 360.00, description: "Pagamento capturado de Mariana Costa para mentoria #2", referenceCode: "TX-2-84120-CAP", createdAt: "2026-10-17T17:50:00" },
        { id: 3, bookingId: 1, type: "PLATFORM_FEE_CREDIT", amount: 27.00, description: "Take-rate 15% retido pela plataforma MentorFlow", referenceCode: "TX-1-71004-FEE", createdAt: "2026-10-16T18:45:00" },
        { id: 2, bookingId: 1, type: "MENTOR_SPLIT_CREDIT", amount: 153.00, description: "Split 85% creditado na carteira do mentor Rafael Mendes", referenceCode: "TX-1-71004-MNT", createdAt: "2026-10-16T18:45:00" },
        { id: 1, bookingId: 1, type: "PAYMENT_CAPTURED", amount: 180.00, description: "Pagamento capturado de Gabriel Almeida para mentoria #1", referenceCode: "TX-1-71004-CAP", createdAt: "2026-10-16T18:45:00" }
    ]
};

function computeDemoMetrics() {
    const captured = demoStore.transactions.filter(t => t.type === 'PAYMENT_CAPTURED').reduce((acc, t) => acc + t.amount, 0);
    const refunded = demoStore.transactions.filter(t => t.type === 'REFUND_DEBIT').reduce((acc, t) => acc + t.amount, 0);
    const netGmv = Math.max(0, captured - refunded);
    const netPlatformRevenue = +(netGmv * 0.15).toFixed(2);
    const netMentorPayouts = +(netGmv - netPlatformRevenue).toFixed(2);

    return {
        totalMentors: demoStore.mentors.length,
        totalMentees: demoStore.mentees.length,
        totalBookings: demoStore.bookings.length,
        confirmedBookings: demoStore.bookings.filter(b => b.status === 'CONFIRMED').length,
        completedBookings: demoStore.bookings.filter(b => b.status === 'COMPLETED').length,
        pendingBookings: demoStore.bookings.filter(b => b.status === 'PENDING_PAYMENT').length,
        cancelledBookings: demoStore.bookings.filter(b => b.status === 'CANCELLED').length,
        grossMerchandiseVolume: netGmv,
        platformNetRevenue: netPlatformRevenue,
        mentorTotalPayouts: netMentorPayouts,
        totalRefunded: refunded,
        recentTransactions: demoStore.transactions
    };
}

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

function handleDemoRequest(method, url, body) {
    const nowIso = new Date().toISOString();

    if (method === 'POST' && url === '/api/v1/bookings') {
        if (body.idempotencyKey && body.idempotencyKey.trim() !== '') {
            const existing = demoStore.bookings.find(b => b.idempotencyKey === body.idempotencyKey.trim());
            if (existing) {
                return { ok: true, status: 201, data: existing };
            }
        }

        const start = new Date(body.startTime);
        const end = new Date(body.endTime);
        const durationMinutes = Math.round((end - start) / 60000);

        if (durationMinutes < 30 || durationMinutes > 180) {
            return {
                ok: false,
                status: 422,
                data: {
                    type: "https://mentorflow.portfolio.dev/errors/business-rule-violation",
                    title: "Violação de Regra de Negócio",
                    status: 422,
                    detail: "A duração da mentoria deve ser de no mínimo 30 minutos e no máximo 180 minutos (3 horas).",
                    instance: "/api/v1/bookings",
                    errorCode: "INVALID_SESSION_DURATION",
                    timestamp: nowIso
                }
            };
        }

        const conflict = demoStore.bookings.find(b =>
            b.mentorId === Number(body.mentorId) &&
            b.status !== 'CANCELLED' &&
            new Date(b.startTime) < end &&
            new Date(b.endTime) > start
        );

        if (conflict) {
            return {
                ok: false,
                status: 409,
                data: {
                    type: "https://mentorflow.portfolio.dev/errors/schedule-conflict",
                    title: "Conflito de Agendamento Detectado (Double-Booking Prevention)",
                    status: 409,
                    detail: `Conflito de agenda: o mentor ${conflict.mentorName} já possui a mentoria #${conflict.id} (${conflict.status}) agendada no intervalo solicitado.`,
                    instance: "/api/v1/bookings",
                    errorCode: "SCHEDULE_OVERLAP_CONFLICT",
                    conflictingBookingId: conflict.id,
                    timestamp: nowIso
                }
            };
        }

        const mentor = demoStore.mentors.find(m => m.id === Number(body.mentorId));
        const mentee = demoStore.mentees.find(s => s.id === Number(body.menteeId));
        const totalAmount = +((mentor.hourlyRate * durationMinutes) / 60).toFixed(2);
        const platformFee = +(totalAmount * 0.15).toFixed(2);
        const mentorAmount = +(totalAmount - platformFee).toFixed(2);

        const newBooking = {
            id: demoStore.bookings.length + 1,
            mentorId: mentor.id,
            mentorName: mentor.name,
            mentorCompany: mentor.company,
            menteeId: mentee.id,
            menteeName: mentee.name,
            topic: body.topic,
            notes: body.notes,
            startTime: body.startTime,
            endTime: body.endTime,
            durationMinutes,
            status: "PENDING_PAYMENT",
            totalAmount,
            mentorAmount,
            platformFee,
            meetingLink: null,
            idempotencyKey: body.idempotencyKey || `bk-${Math.random().toString(36).substring(2, 10)}`
        };
        demoStore.bookings.unshift(newBooking);
        return { ok: true, status: 201, data: newBooking };
    }

    if (method === 'POST' && url.endsWith('/confirm-payment')) {
        const id = Number(url.split('/')[4]);
        const booking = demoStore.bookings.find(b => b.id === id);
        const mentor = demoStore.mentors.find(m => m.id === booking.mentorId);
        const mentee = demoStore.mentees.find(s => s.id === booking.menteeId);

        if (mentee.walletBalance < booking.totalAmount) {
            return {
                ok: false,
                status: 422,
                data: {
                    type: "https://mentorflow.portfolio.dev/errors/business-rule-violation",
                    title: "Violação de Regra de Negócio",
                    status: 422,
                    detail: `Saldo insuficiente na carteira de ${mentee.name}. Necessário: R$ ${booking.totalAmount.toFixed(2)} | Disponível: R$ ${mentee.walletBalance.toFixed(2)}`,
                    instance: url,
                    errorCode: "INSUFFICIENT_WALLET_BALANCE",
                    timestamp: nowIso
                }
            };
        }

        mentee.walletBalance = +(mentee.walletBalance - booking.totalAmount).toFixed(2);
        mentor.walletBalance = +(mentor.walletBalance + booking.mentorAmount).toFixed(2);
        booking.status = 'CONFIRMED';
        booking.meetingLink = `https://meet.mentorflow.dev/sala-${booking.id}-live`;

        const ref = `TX-${booking.id}-${Math.floor(10000 + Math.random() * 89999)}`;
        demoStore.transactions.unshift(
            { id: Date.now() + 3, bookingId: booking.id, type: "PLATFORM_FEE_CREDIT", amount: booking.platformFee, description: "Take-rate 15% retido pela plataforma MentorFlow", referenceCode: `${ref}-FEE`, createdAt: nowIso },
            { id: Date.now() + 2, bookingId: booking.id, type: "MENTOR_SPLIT_CREDIT", amount: booking.mentorAmount, description: `Split 85% creditado na carteira do mentor ${mentor.name}`, referenceCode: `${ref}-MNT`, createdAt: nowIso },
            { id: Date.now() + 1, bookingId: booking.id, type: "PAYMENT_CAPTURED", amount: booking.totalAmount, description: `Pagamento capturado de ${mentee.name} para mentoria #${booking.id}`, referenceCode: `${ref}-CAP`, createdAt: nowIso }
        );
        return { ok: true, status: 200, data: booking };
    }

    if (method === 'POST' && url.endsWith('/complete')) {
        const id = Number(url.split('/')[4]);
        const booking = demoStore.bookings.find(b => b.id === id);
        if (new Date() < new Date(booking.startTime)) {
            return {
                ok: false,
                status: 422,
                data: {
                    type: "https://mentorflow.portfolio.dev/errors/business-rule-violation",
                    title: "Violação de Regra de Negócio",
                    status: 422,
                    detail: `Não é possível concluir uma sessão futura antes do seu início (${booking.startTime}).`,
                    instance: url,
                    errorCode: "PREMATURE_SESSION_COMPLETION",
                    timestamp: nowIso
                }
            };
        }
        const mentor = demoStore.mentors.find(m => m.id === booking.mentorId);
        booking.status = 'COMPLETED';
        mentor.totalSessionsCompleted += 1;
        return { ok: true, status: 200, data: booking };
    }

    if (method === 'POST' && url.endsWith('/cancel')) {
        const id = Number(url.split('/')[4]);
        const booking = demoStore.bookings.find(b => b.id === id);
        if (booking.status === 'CONFIRMED') {
            const mentor = demoStore.mentors.find(m => m.id === booking.mentorId);
            const mentee = demoStore.mentees.find(s => s.id === booking.menteeId);
            mentee.walletBalance = +(mentee.walletBalance + booking.totalAmount).toFixed(2);
            mentor.walletBalance = +(mentor.walletBalance - booking.mentorAmount).toFixed(2);
            demoStore.transactions.unshift({
                id: Date.now(),
                bookingId: booking.id,
                type: "REFUND_DEBIT",
                amount: booking.totalAmount,
                description: `Estorno integral ao mentorado ${mentee.name} por cancelamento`,
                referenceCode: `TX-REF-${booking.id}`,
                createdAt: nowIso
            });
        }
        booking.status = 'CANCELLED';
        return { ok: true, status: 200, data: booking };
    }

    if (method === 'POST' && url.includes('/topup')) {
        const id = Number(url.split('/')[4]);
        const mentee = demoStore.mentees.find(s => s.id === id);
        mentee.walletBalance = +(mentee.walletBalance + 500).toFixed(2);
        return { ok: true, status: 200, data: mentee };
    }

    return { ok: true, status: 200, data: {} };
}

async function apiRequest(method, url, body = null) {
    if (state.demoMode) {
        const sim = handleDemoRequest(method, url, body);
        logToInspector(method, url, sim.status, sim.data);
        return sim;
    }

    try {
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
    } catch (err) {
        state.demoMode = true;
        const sim = handleDemoRequest(method, url, body);
        logToInspector(method, url, sim.status, sim.data);
        return sim;
    }
}

async function refreshAll() {
    const specialty = document.getElementById('specialtyFilter').value;
    const mentorQuery = specialty ? `?specialty=${encodeURIComponent(specialty)}` : '';

    try {
        if (state.demoMode) throw new Error('Static Demo Mode');
        const metricsCheck = await fetch('/api/v1/dashboard/metrics');
        if (!metricsCheck.ok) throw new Error('Static host detected');

        const [metricsRes, mentorsRes, menteesRes, bookingsRes] = await Promise.all([
            metricsCheck.json(),
            fetch(`/api/v1/mentors${mentorQuery}`).then(r => r.json()),
            fetch('/api/v1/mentees').then(r => r.json()),
            fetch('/api/v1/bookings').then(r => r.json())
        ]);

        state.metrics = metricsRes;
        state.mentors = mentorsRes;
        state.mentees = menteesRes;
        state.bookings = bookingsRes;
    } catch (e) {
        state.demoMode = true;
        state.metrics = computeDemoMetrics();
        state.mentors = specialty
            ? demoStore.mentors.filter(m => m.specialty === specialty)
            : [...demoStore.mentors];
        state.mentees = [...demoStore.mentees];
        state.bookings = [...demoStore.bookings];
    }

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

document.getElementById('btnSimulateConflict').addEventListener('click', async () => {
    const activeBooking = state.bookings.find(b => b.status !== 'CANCELLED');
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

const defaultDate = new Date(Date.now() + 5 * 86400000).toISOString().split('T')[0];
document.getElementById('formDate').value = defaultDate;

refreshAll();
