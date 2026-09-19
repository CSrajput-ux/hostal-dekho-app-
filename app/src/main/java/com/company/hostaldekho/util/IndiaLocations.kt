package com.company.hostaldekho.util

data class IndiaState(
    val name: String,
    val isUnionTerritory: Boolean = false,
    val districts: List<String>
)

object IndiaLocations {
    val states = listOf(
        IndiaState(
            name = "Andhra Pradesh",
            districts = listOf("Visakhapatnam", "Vijayawada", "Guntur", "Tirupati", "Kakinada", "Nellore", "Kurnool", "Anantapur", "Rajahmundry", "Eluru", "Kadapa")
        ),
        IndiaState(
            name = "Arunachal Pradesh",
            districts = listOf("Itanagar", "Naharlagun", "Pasighat", "Tawang", "Ziro", "Tezu")
        ),
        IndiaState(
            name = "Assam",
            districts = listOf("Guwahati", "Silchar", "Dibrugarh", "Jorhat", "Nagaon", "Tezpur", "Tinsukia", "Bongaigaon")
        ),
        IndiaState(
            name = "Bihar",
            districts = listOf("Patna (Boring Road)", "Patna (Kankarbagh)", "Gaya", "Muzaffarpur", "Bhagalpur", "Darbhanga", "Purnia", "Rohtas", "Arrah", "Begusarai", "Katihar", "Munger", "Chhapra")
        ),
        IndiaState(
            name = "Chhattisgarh",
            districts = listOf("Raipur", "Bhilai", "Bilaspur", "Korba", "Durg", "Rajnandgaon", "Jagdalpur")
        ),
        IndiaState(
            name = "Goa",
            districts = listOf("North Goa (Panaji)", "South Goa (Margao)", "Vasco da Gama", "Mapusa")
        ),
        IndiaState(
            name = "Gujarat",
            districts = listOf("Ahmedabad", "Surat", "Vadodara", "Rajkot", "Bhavnagar", "Jamnagar", "Gandhinagar", "Junagadh", "Anand", "Navsari", "Morbi")
        ),
        IndiaState(
            name = "Haryana",
            districts = listOf("Gurugram", "Faridabad", "Panipat", "Ambala", "Karnal", "Hisar", "Rohtak", "Sonipat", "Panchkula", "Kurukshetra")
        ),
        IndiaState(
            name = "Himachal Pradesh",
            districts = listOf("Shimla", "Dharamshala", "Mandi", "Solan", "Kullu", "Manali", "Hamirpur", "Kangra", "Bilaspur")
        ),
        IndiaState(
            name = "Jharkhand",
            districts = listOf("Ranchi", "Jamshedpur", "Dhanbad", "Bokaro", "Hazaribagh", "Deoghar", "Giridih")
        ),
        IndiaState(
            name = "Karnataka",
            districts = listOf("Bengaluru (Bangalore)", "Mysuru (Mysore)", "Mangaluru", "Hubballi-Dharwad", "Belagavi", "Kalaburagi", "Shivamogga", "Tumakuru", "Udupi")
        ),
        IndiaState(
            name = "Kerala",
            districts = listOf("Thiruvananthapuram", "Kochi (Cochin)", "Kozhikode", "Thrissur", "Kollam", "Kannur", "Alappuzha", "Kottayam", "Palakkad", "Malappuram")
        ),
        IndiaState(
            name = "Madhya Pradesh",
            districts = listOf("Indore", "Bhopal", "Gwalior", "Jabalpur", "Ujjain", "Sagar", "Rewa", "Satna", "Ratlam", "Singrauli")
        ),
        IndiaState(
            name = "Maharashtra",
            districts = listOf("Mumbai", "Pune (Kothrud/Hinjewadi)", "Nagpur", "Thane", "Nashik", "Chhatrapati Sambhajinagar", "Solapur", "Kharghar (Navi Mumbai)", "Kolhapur", "Amravati")
        ),
        IndiaState(
            name = "Manipur",
            districts = listOf("Imphal", "Churachandpur", "Thoubal", "Bishnupur")
        ),
        IndiaState(
            name = "Meghalaya",
            districts = listOf("Shillong", "Tura", "Jowai", "Nongpoh")
        ),
        IndiaState(
            name = "Mizoram",
            districts = listOf("Aizawl", "Lunglei", "Champhai")
        ),
        IndiaState(
            name = "Nagaland",
            districts = listOf("Kohima", "Dimapur", "Mokokchung")
        ),
        IndiaState(
            name = "Odisha",
            districts = listOf("Bhubaneswar", "Cuttack", "Rourkela", "Berhampur", "Sambalpur", "Puri", "Balasore")
        ),
        IndiaState(
            name = "Punjab",
            districts = listOf("Ludhiana", "Amritsar", "Jalandhar", "Patiala", "Bathinda", "Mohali (SAS Nagar)", "Pathankot", "Hoshiarpur")
        ),
        IndiaState(
            name = "Rajasthan",
            districts = listOf("Kota (Coaching Hub)", "Jaipur", "Jodhpur", "Udaipur", "Ajmer", "Bikaner", "Alwar", "Bhilwara", "Sikar (Coaching Hub)", "Bharatpur")
        ),
        IndiaState(
            name = "Sikkim",
            districts = listOf("Gangtok", "Namchi", "Gyalshing")
        ),
        IndiaState(
            name = "Tamil Nadu",
            districts = listOf("Chennai", "Coimbatore", "Madurai", "Tiruchirappalli", "Salem", "Tiruppur", "Erode", "Vellore", "Tirunelveli")
        ),
        IndiaState(
            name = "Telangana",
            districts = listOf("Hyderabad (Ameerpet/Madhapur)", "Warangal", "Nizamabad", "Karimnagar", "Khammam", "Ramagundam")
        ),
        IndiaState(
            name = "Tripura",
            districts = listOf("Agartala", "Udaipur", "Dharmanagar")
        ),
        IndiaState(
            name = "Uttar Pradesh",
            districts = listOf("Noida", "Greater Noida", "Lucknow", "Kanpur (Kakadeo)", "Varanasi", "Prayagraj (Allahabad)", "Agra", "Ghaziabad", "Meerut", "Gorakhpur", "Bareilly", "Aligarh", "Jhansi", "Mathura", "Ayodhya")
        ),
        IndiaState(
            name = "Uttarakhand",
            districts = listOf("Dehradun", "Haridwar", "Roorkee", "Haldwani", "Rishikesh", "Nainital", "Almora")
        ),
        IndiaState(
            name = "West Bengal",
            districts = listOf("Kolkata", "Howrah", "Durgapur", "Asansol", "Siliguri", "Kharagpur", "Bardhaman")
        ),
        // Union Territories
        IndiaState(
            name = "Delhi NCR",
            isUnionTerritory = true,
            districts = listOf("North Delhi (Mukherjee Nagar)", "South Delhi (Kalu Sarai)", "East Delhi (Laxmi Nagar)", "West Delhi (Janakpuri)", "Central Delhi", "Noida", "Gurugram")
        ),
        IndiaState(
            name = "Chandigarh",
            isUnionTerritory = true,
            districts = listOf("Chandigarh Sector 15", "Chandigarh Sector 34", "Chandigarh Sector 36", "Mohali", "Panchkula")
        ),
        IndiaState(
            name = "Jammu and Kashmir",
            isUnionTerritory = true,
            districts = listOf("Srinagar", "Jammu", "Anantnag", "Baramulla", "Udhampur")
        ),
        IndiaState(
            name = "Ladakh",
            isUnionTerritory = true,
            districts = listOf("Leh", "Kargil")
        ),
        IndiaState(
            name = "Puducherry",
            isUnionTerritory = true,
            districts = listOf("Puducherry", "Karaikal", "Mahe", "Yanam")
        ),
        IndiaState(
            name = "Andaman & Nicobar",
            isUnionTerritory = true,
            districts = listOf("Port Blair")
        ),
        IndiaState(
            name = "Dadra & Nagar Haveli and Daman & Diu",
            isUnionTerritory = true,
            districts = listOf("Daman", "Diu", "Silvassa")
        ),
        IndiaState(
            name = "Lakshadweep",
            isUnionTerritory = true,
            districts = listOf("Kavaratti")
        )
    )

    val topHostelHubs = listOf(
        "All India",
        "Kota, Rajasthan",
        "Patna, Bihar",
        "Delhi (Mukherjee Nagar)",
        "Delhi (Laxmi Nagar)",
        "Delhi (Kalu Sarai)",
        "Noida / Greater Noida",
        "Gurugram, Haryana",
        "Indore, MP",
        "Bhopal, MP",
        "Jaipur, Rajasthan",
        "Sikar, Rajasthan",
        "Lucknow, UP",
        "Prayagraj, UP",
        "Kanpur, UP",
        "Varanasi, UP",
        "Pune, Maharashtra",
        "Mumbai, Maharashtra",
        "Bengaluru, Karnataka",
        "Hyderabad, Telangana",
        "Dehradun, Uttarakhand",
        "Chandigarh / Mohali"
    )

    fun getAllDistrictsFormatted(): List<String> {
        return states.flatMap { state ->
            state.districts.map { "$it, ${state.name}" }
        }
    }
}
