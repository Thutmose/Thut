local function write_gamelog(msg)
    local log = io.open('constructs.txt', 'a')
    log:write(msg.."\n")
    log:close()
end

local function fullname(item)
    return dfhack.TranslateName(item.name)..' ('..dfhack.TranslateName(item.name ,true)..')'
end

function tablelength(T)
  local count = 0
  for _ in pairs(T) do count = count + 1 end
  return count
end

local args = {...}

local i = 1
local construct = df.world_construction.find(i)

----[[
while not df.isnull(construct) do
	
	local num = tablelength(construct.square_obj)
	
	local message = i..''
	
	for j = 0, num - 1, 1 do
		local square_obj = construct.square_obj[j]
		local region_pos = square_obj.region_pos
		local embark_x = square_obj.embark_x
		local embark_y = square_obj.embark_y
		local embark_z = square_obj.embark_z
		local zcount = tablelength(embark_z)
		for k = 0, tablelength(embark_x)-1, 1 do
			local x = region_pos.x * 16 + embark_x[k]
			local y = region_pos.y * 16 + embark_y[k]
			local z = -1
			if zcount > 0 then
				z = embark_z[k]
			end
			message = message .. ':'..x..','..z..','..y..''
		end
	end
	
	if num > 0 then
		write_gamelog(message)
	end
	
	i = i + 1
	construct = df.world_construction.find(i)
end --]]


