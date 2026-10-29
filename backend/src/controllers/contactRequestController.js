const ContactRequest = require('../models/ContactRequest');
const Donation = require('../models/Donation');
const User = require('../models/User');
const Notification = require('../models/Notification');

// POST /api/contact-requests — Send a contact/number request to the donor
exports.sendRequest = async (req, res) => {
    try {
        const requesterId = req.userId;
        const { donationId } = req.body;

        if (!donationId) {
            return res.status(400).json({ success: false, message: 'donationId required' });
        }

        // Fetch donation and extract donor ID
        const donation = await Donation.findById(donationId);
        if (!donation) {
            return res.status(404).json({ success: false, message: 'Donation not found' });
        }

        const donorId = donation.user;

        // Cannot request your own donation
        if (donorId.toString() === requesterId.toString()) {
            return res.status(400).json({ success: false, message: 'You cannot request your own donation.' });
        }

        // If a request already exists, return it
        const existing = await ContactRequest.findOne({ requester: requesterId, donation: donationId });
        if (existing) {
            return res.json({ success: true, data: existing, message: 'Request already send' });
        }

        const request = await ContactRequest.create({
            requester: requesterId,
            donor: donorId,
            donation: donationId
        });

        console.log(`📨 Contact request sent: ${requesterId} → donor ${donorId}`);

        // Create notification for donor
        await Notification.create({
            recipient: donorId,
            sender: requesterId,
            type: 'contact_request',
            message: 'Someone requested your blood donation',
            relatedId: request._id
        });

        res.status(201).json({ success: true, data: request });
    } catch (error) {
        console.error('❌ sendRequest error:', error.message);
        res.status(500).json({ success: false, message: error.message });
    }
};

// GET /api/contact-requests/received — Get incoming requests for the donor
exports.getReceivedRequests = async (req, res) => {
    try {
        const donorId = req.userId;

        const requests = await ContactRequest.find({ donor: donorId })
            .populate('requester', 'name bloodGroup city')
            .populate('donation', 'bloodGroup city message')
            .sort({ createdAt: -1 });

        res.json({ success: true, count: requests.length, data: requests });
    } catch (error) {
        console.error('❌ getReceivedRequests error:', error.message);
        res.status(500).json({ success: false, message: error.message });
    }
};

// PUT /api/contact-requests/:id — Accept or reject a contact request
exports.respondToRequest = async (req, res) => {
    try {
        const donorId = req.userId;
        const { id } = req.params;
        const { action } = req.body; // 'accepted' or 'rejected'

        if (!['accepted', 'rejected'].includes(action)) {
            return res.status(400).json({ success: false, message: "Action must be 'accepted' or 'rejected'" });
        }

        const request = await ContactRequest.findById(id);
        if (!request) {
            return res.status(404).json({ success: false, message: 'Request not found' });
        }

        // Only the donor can respond to this request
        if (request.donor.toString() !== donorId.toString()) {
            return res.status(403).json({ success: false, message: 'Unauthorized' });
        }

        request.status = action;
        await request.save();

        console.log(`✅ Contact request ${action}: ID ${id}`);

        // Create notification for requester
        await Notification.create({
            recipient: request.requester,
            sender: donorId,
            type: action === 'accepted' ? 'request_accepted' : 'request_rejected',
            message: `Your contact request was ${action}`,
            relatedId: request._id
        });

        res.json({ success: true, data: request });
    } catch (error) {
        console.error('❌ respondToRequest error:', error.message);
        res.status(500).json({ success: false, message: error.message });
    }
};

// GET /api/contact-requests/status/:donationId — Check the status of a contact request
exports.getRequestStatus = async (req, res) => {
    try {
        const requesterId = req.userId;
        const { donationId } = req.params;

        const request = await ContactRequest.findOne({ requester: requesterId, donation: donationId });

        if (!request) {
            return res.json({ success: true, data: { status: 'none' } });
        }

        let phone = null;
        // If accepted, also include the phone number
        if (request.status === 'accepted') {
            const donor = await User.findById(request.donor).select('phone');
            phone = donor ? donor.phone : null;
        }

        res.json({ success: true, data: { status: request.status, requestId: request._id, phone } });
    } catch (error) {
        console.error('❌ getRequestStatus error:', error.message);
        res.status(500).json({ success: false, message: error.message });
    }
};
